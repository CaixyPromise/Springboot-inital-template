package com.caixy.adminSystem.authorization.service.utils;

import cn.hutool.crypto.digest.DigestUtil;
import com.caixy.adminSystem.common.base.annotation.StaticValue;
import com.caixy.adminSystem.common.base.annotation.StaticValueTarget;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.response.ErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Token工具类
 *
 * @Author CAIXYPROMISE
 * @since 2025/4/25 3:06
 */
@StaticValueTarget
public class TokenUtils
{
    private static final Logger log = LoggerFactory.getLogger(TokenUtils.class);
    @StaticValue(value = "encryption.key", defaultValue = "caixypromise", onSucceed = "onInjectSecretKey", parameterized = false)
    private static String secretKey;
    private static Key keyPair;

    public static final Duration FIVE_MINUTE = Duration.ofMinutes(5);
    public static final Duration TEN_MINUTE = Duration.ofMinutes(10);
    public static final Duration FIFTEEN_MINUTE = Duration.ofMinutes(15);
    public static final Duration THIRTY_MINUTE = Duration.ofMinutes(30);
    public static final Duration ONE_HOUR = Duration.ofHours(1);
    public static final Duration ONE_DAY = Duration.ofDays(1);

    private static void onInjectSecretKey()
    {
        keyPair = new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS256.getJcaName());
    }

    public static String createMD5Token(String... args)
    {
        return createMD5Token(String.join("-", args));
    }

    public static String createMD5TokenWithSalt(String... args)
    {
        StringJoiner joiner = new StringJoiner("-");
        Arrays.stream(args).forEach(joiner::add);
        joiner.add(secretKey)
                .add(UUID.randomUUID().toString());
        return createMD5Token(joiner.toString());
    }

    public static String createMD5Token(String content)
    {
        return DigestUtil.md5Hex(content);
    }


    public static String createJWTToken(Map<String, Object> claims, Duration ttl)
    {
        return createJWTToken(claims, UUID.randomUUID().toString(), ttl);
    }


    public static String createJWTToken(Map<String, Object> claims, String subject, Duration ttl)
    {
        return createJWTToken(claims, subject, createMD5Token(subject, UUID.randomUUID().toString(), String.valueOf(System.currentTimeMillis())), ttl);
    }

    public static String createJWTToken(Map<String, Object> claims, String subject, String jwtId, Duration ttl)
    {
        Instant now = Instant.now();
        Instant exp = now.plus(ttl);

        return Jwts.builder().setSubject(subject).setIssuedAt(Date.from(now)).setExpiration(Date.from(exp)).setClaims(claims).signWith(keyPair, SignatureAlgorithm.HS256).setId(jwtId).compact();
    }

    /**
     * 验证 JWT Token
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/4/25 15:52
     */
    public static Claims verifyJWTToken(String token)
    {
        try
        {
            return Jwts.parserBuilder().setSigningKey(keyPair).build().parseClaimsJws(token).getBody();
        }
        catch (JwtException e)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "凭证过期");
        }
    }

    public static Map<String, Object> parseToken(String token)
    {
        return verifyJWTToken(token);
    }

    /**
     * JWT是否过期
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/4/25 15:52
     */
    public static boolean isJWTExpired(String token)
    {
        try
        {
            Claims claims = verifyJWTToken(token);
            return claims.getExpiration().before(new Date());
        }
        catch (BusinessException e)
        {
            return true;
        }
    }

    /**
     * 刷新 Token（重新生成，继承原始 claims 和 subject）
     */
    public static String refreshJWTToken(String token, Duration newTtl)
    {
        Claims claims = verifyJWTToken(token);
        String subject = claims.getSubject();
        claims.remove(Claims.EXPIRATION);
        claims.remove(Claims.ISSUED_AT);
        claims.remove(Claims.NOT_BEFORE);
        claims.remove(Claims.ID);

        return createJWTToken(claims, subject, newTtl);
    }


}
