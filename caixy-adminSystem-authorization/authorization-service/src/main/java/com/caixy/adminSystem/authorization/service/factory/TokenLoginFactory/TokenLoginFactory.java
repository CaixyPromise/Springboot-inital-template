package com.caixy.adminSystem.authorization.service.factory.TokenLoginFactory;


import com.caixy.adminSystem.authorization.service.factory.AuthorizationFactory;
import com.caixy.adminSystem.authorization.service.factory.TokenLoginFactory.properties.TokenProperties;
import com.caixy.adminSystem.common.api.user.vo.LoginUserVO;
import com.caixy.adminSystem.common.api.user.vo.UserVO;
import com.caixy.adminSystem.common.base.constant.UserRoleEnum;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.response.ErrorCode;
import com.caixy.adminSystem.common.base.utils.ServletUtils;
import com.caixy.adminSystem.common.base.utils.StringUtils;
import com.caixy.adminSystem.infrastucture.cache.redis.RedisManager;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Token登录验证服务
 *
 * @author CAIXYPROMISE
 * @since 2024/10/27 02:08
 */
@Service
@ConditionalOnProperty(name = "login.type", havingValue = "TOKEN")
@Slf4j
public class TokenLoginFactory implements AuthorizationFactory
{
    private final boolean singleLogin;
    private final String headerKey;
    private final String secret;
    private final long expireTime;
    private final TimeUnit expireTimeUnit;
    private final long refreshTime;
    private final TimeUnit refreshTimeUnit;
    private static final String TOKEN_CACHE_KEY = "token:login:";
    private static final String TOKEN_CLAIMS_ID_KEY = "tokenId";
    private static final String TOKEN_CLAIMS_USER_ID_KEY = "userId";
    private static final Pattern pattern = Pattern.compile("^(?:Bearer\\s)?(.+)$");
    // Redis中存储所有活跃tokenId的集合key
    private static final String TOKEN_ACTIVE_SET = "token:activeTokens";

    // Redis中存储用户与tokenId映射的key前缀
    private static final String TOKEN_USER_KEY_PREFIX = "token:user:";

    private final RedisManager redisManager;

    public TokenLoginFactory(TokenProperties tokenProperties, RedisManager redisManager)
    {
        this.singleLogin = tokenProperties.isSingleLogin();
        this.headerKey = tokenProperties.getHeader();
        this.secret = tokenProperties.getSecret();
        this.expireTime = tokenProperties.getExpireTime();
        this.expireTimeUnit = tokenProperties.getExpireTimeUnit();
        this.refreshTime = tokenProperties.getRefreshTime();
        this.refreshTimeUnit = tokenProperties.getRefreshTimeUnit();
        Key key = new SecretKeySpec(secret.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        this.redisManager = redisManager;
    }




    @Override
    public String getName()
    {
        return "Token验证服务";
    }

    @Override
    public Boolean checkLogin(HttpServletRequest request)
    {
        String token = extractToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            UserVO userVO = getUserVOFromCache(token);
            return userVO != null;
        }
        return false;
    }

    @Override
    public Boolean checkLogin()
    {
        return checkLogin(ServletUtils.getRequest());
    }

    @Override
    public Boolean asRole(UserRoleEnum roleEnum)
    {
        UserVO userVO = getLoginUserPermitNull(ServletUtils.getRequest());
        return userVO != null && userVO.getUserRole().equals(roleEnum);
    }

    @Override
    public void checkLoginOrThrow()
    {
        UserVO userVO = getLoginUserPermitNull(ServletUtils.getRequest());
        if (userVO == null)
        {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "未登录");
        }
    }

    @Override
    public LoginUserVO doLogin(UserVO userVO, HttpServletRequest request)
    {
        TokenInfo tokenInfo = createToken(userVO);
        String token = tokenInfo.getToken();
        String tokenId = tokenInfo.getTokenId();

        userVO.setToken(token);
        userVO.setLoginTime(System.currentTimeMillis());
        userVO.setExpireTime(userVO.getLoginTime() + expireTime * 60 * 1000);

        // 设置登录信息
        setUserLoginInfo(userVO, request);


        // 处理单点登录
        if (singleLogin)
        {
            // 强制下线之前的登录
            forceLogout(userVO.getId());
        }

        // 存储token到Redis
        redisManager.setObject(getTokenCacheKey(tokenId), userVO, expireTime, expireTimeUnit);

        // 将tokenId添加到活跃token集合
        redisManager.addToSet(TOKEN_ACTIVE_SET, tokenId);

        // 将tokenId添加到用户的token集合
        String userTokenKey = getUserTokenKey(userVO.getId());
        redisManager.addToSet(userTokenKey, tokenId);
        return getLoginUserVO(userVO);
    }


    @Override
    public boolean doLogout()
    {
        HttpServletRequest request = ServletUtils.getRequest();
        String token = extractToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            Map<String, Object> claims = parseToken(token);
            if (claims != null)
            {
                String tokenId = (String) claims.get(TOKEN_CLAIMS_ID_KEY);
                Long userId = ((Number) claims.get(TOKEN_CLAIMS_USER_ID_KEY)).longValue();
                if (StringUtils.isNotEmpty(tokenId))
                {
                    // 删除Redis中的token信息
                    redisManager.delete(getTokenCacheKey(tokenId));
                    // 从活跃token集合中移除
                    redisManager.removeFromSet(TOKEN_ACTIVE_SET, tokenId);
                    // 从用户的token集合中移除
                    String userTokenKey = getUserTokenKey(userId);
                    redisManager.removeFromSet(userTokenKey, tokenId);
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public UserVO getLoginUser(HttpServletRequest request)
    {
        UserVO loginUser = getLoginUserPermitNull(request);
        if (loginUser == null)
        {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "未登录");
        }
        return loginUser;
    }

    @Override
    public UserVO getLoginUserPermitNull(HttpServletRequest request)
    {
        String token = extractToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            UserVO userVO = getUserVOFromCache(token);
            if (userVO != null)
            {
                try
                {
                    checkExpireTime(userVO, token);
                }
                catch (Exception e)
                {
                    log.error("Token检查过期时间异常：{}", e.getMessage(), e);
                    return null;
                }
                return userVO;
            }
        }
        return null;
    }

    @Override
    public Boolean isAdmin()
    {
        return asRole(UserRoleEnum.ADMIN);
    }

    @Override
    public List<UserVO> getAllLoggedInUsers(int currentSize, int size)
    {
        Set<String> tokenIds = redisManager.getMembersFromSet(TOKEN_ACTIVE_SET);
        List<UserVO> userList = new ArrayList<>();
        if (tokenIds != null && !tokenIds.isEmpty())
        {
            List<String> tokenIdList = new ArrayList<>(tokenIds);

            // 实现分页
            int totalTokens = tokenIdList.size();
            int fromIndex = (currentSize - 1) * size;
            int toIndex = Math.min(fromIndex + size, totalTokens);
            if (fromIndex >= totalTokens)
            {
                return userList; // 返回空列表
            }
            List<String> pagedTokenIds = tokenIdList.subList(fromIndex, toIndex);

            for (String tokenId : pagedTokenIds)
            {
                Optional<UserVO> userVOOptional = redisManager.getObject(getTokenCacheKey(tokenId), UserVO.class);
                if (userVOOptional.isPresent())
                {
                    userList.add(userVOOptional.get());
                }
                else
                {
                    // 如果缓存中没有找到对应的 UserVO，可能是过期的 tokenId，需要移除
                    redisManager.removeFromSet(TOKEN_ACTIVE_SET, tokenId);
                }
            }
        }
        return userList;
    }

    @Override
    public int getLoggedInUserCount()
    {
        Long size = redisManager.getSetSize(TOKEN_ACTIVE_SET);
        return size != null ? size.intValue() : 0;
    }

    @Override
    public void forceLogout(Long userId)
    {
        String userTokenKey = getUserTokenKey(userId);
        Set<String> tokenIds = redisManager.getMembersFromSet(userTokenKey);
        if (tokenIds != null && !tokenIds.isEmpty())
        {
            // 批量删除 Token 信息
            List<String> keysToDelete = tokenIds.stream()
                                                .map(this::getTokenCacheKey)
                                                .collect(Collectors.toList());
            redisManager.delete(keysToDelete);
            // 从活跃 Token 集合中移除
            redisManager.removeFromSet(TOKEN_ACTIVE_SET, tokenIds.toArray(new String[0]));
            // 删除用户的 Token 集合
            redisManager.delete(userTokenKey);
        }

    }


    private TokenInfo createToken(UserVO user)
    {
        String tokenId = UUID.randomUUID().toString();

        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_CLAIMS_ID_KEY, tokenId);
        claims.put(TOKEN_CLAIMS_USER_ID_KEY, user.getId());

        String token = createToken(claims);

        return new TokenInfo(token, tokenId);
    }

    private String createToken(Map<String, Object> claims)
    {
        return Jwts.builder()
                   .setClaims(claims)
                   .setExpiration(new Date(System.currentTimeMillis() + expireTime * 60 * 1000))
                   .signWith(SignatureAlgorithm.HS512, secret)
                   .compact();
    }

    private void refreshToken(UserVO userVO, String tokenId)
    {
        userVO.setLoginTime(System.currentTimeMillis());
        userVO.setExpireTime(userVO.getLoginTime() + expireTime * 60 * 1000);

        redisManager.setObject(getTokenCacheKey(tokenId), userVO, expireTime, expireTimeUnit); // 转换为秒
    }


    private String getTokenCacheKey(String tokenId)
    {
        return TOKEN_CACHE_KEY + tokenId;
    }

    private void checkExpireTime(UserVO userVO, String token)
    {
        try
        {
            Map<String, Object> claims = parseToken(token);
            if (claims != null)
            {
                String tokenId = (String) claims.get(TOKEN_CLAIMS_ID_KEY);
                if (StringUtils.isNotEmpty(tokenId))
                {
                    Long expire = redisManager.getExpire(getTokenCacheKey(tokenId));

                    long refreshThreshold = refreshTimeUnit.toSeconds(refreshTime);
                    if (expire != null && expire < refreshThreshold) {
                        refreshToken(userVO, tokenId);
                        log.info("Token即将过期，已自动刷新");
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("Error in checkExpireTime: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录过期");
        }
    }


    private Map<String, Object> parseToken(String token)
    {
        try
        {
            return Jwts.parser()
                       .setSigningKey(secret)
                       .parseClaimsJws(token)
                       .getBody();
        }
        catch (ExpiredJwtException e)
        {
            log.warn("Token已过期：{}", token);
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "Token已过期");
        }
        catch (JwtException e)
        {
            log.warn("Token无效：{}", token);
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "Token无效");
        }
        catch (Exception e)
        {
            log.error("Token解析异常：{}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Token解析异常");
        }
    }


    private UserVO getUserVOFromCache(String token)
    {
        if (StringUtils.isNotEmpty(token))
        {
            Map<String, Object> claims = parseToken(token);
            if (claims != null)
            {
                String tokenId = (String) claims.get(TOKEN_CLAIMS_ID_KEY);
                if (StringUtils.isNotEmpty(tokenId))
                {
                    Optional<UserVO> userVOOptional = redisManager.getObject(getTokenCacheKey(tokenId), UserVO.class);
                    return userVOOptional.orElse(null);
                }
            }
        }
        return null;
    }


    private String getUserTokenKey(Long userId)
    {
        return TOKEN_USER_KEY_PREFIX + userId;
    }

    /**
     * 从Header提取token
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/31 上午1:20
     */
    private String extractToken(HttpServletRequest request)
    {
        String token = request.getHeader(headerKey);
        if (StringUtils.isNotEmpty(token))
        {
            // 正则表达式提取token
            Matcher matcher = pattern.matcher(token);
            if (matcher.find())
            {
                return matcher.group(1);
            }
            else
            {
                return null;
            }
        }
        return null;
    }


    @Data
    private static class TokenInfo
    {
        private String token;
        private String tokenId;

        public TokenInfo(String token, String tokenId)
        {
            this.token = token;
            this.tokenId = tokenId;
        }
    }
}
