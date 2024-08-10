package com.caixy.adminSystem.manager.oauth;

import cn.hutool.core.util.RandomUtil;
import com.caixy.adminSystem.annotation.InjectOAuthConfig;
import com.caixy.adminSystem.annotation.OAuthTypeTarget;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.config.properties.OAuth2ClientProperties;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.model.dto.oauth.OAuthResultResponse;
import com.caixy.adminSystem.model.dto.oauth.github.GithubCallbackRequest;
import com.caixy.adminSystem.model.dto.oauth.github.GithubCallbackResponse;
import com.caixy.adminSystem.model.dto.oauth.github.GithubGetAuthorizationUrlRequest;
import com.caixy.adminSystem.model.dto.oauth.github.GithubUserProfileDTO;
import com.caixy.adminSystem.model.dto.user.UserLoginByOAuthAdapter;
import com.caixy.adminSystem.model.entity.User;
import com.caixy.adminSystem.model.enums.OAuthProviderEnum;
import com.caixy.adminSystem.model.enums.RedisConstant;
import com.caixy.adminSystem.model.enums.UserGenderEnum;
import com.caixy.adminSystem.model.enums.UserRoleEnum;
import com.caixy.adminSystem.strategy.OAuth2ActionStrategy;
import com.caixy.adminSystem.utils.HttpUtils;
import com.caixy.adminSystem.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * GithubOAuth验证实现类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.oauth.GithubOAuthActionStrategyIml
 * @since 2024/8/3 上午1:04
 */
@Slf4j
@Component
@OAuthTypeTarget(clientName = OAuthProviderEnum.GITHUB)
public class GithubOAuthActionStrategyImpl extends OAuth2ActionStrategy<
        GithubCallbackResponse,
        GithubUserProfileDTO,
        GithubGetAuthorizationUrlRequest,
        GithubCallbackRequest>
{
    @InjectOAuthConfig(clientName = OAuthProviderEnum.GITHUB)
    private OAuth2ClientProperties.OAuth2Client githubClient;

    private static final String STATE_KEY = "state";
    private static final String USER_REDIRECT_URI_KEY = "user_redirect_uri";


    @Override
    public String getAuthorizationUrl(GithubGetAuthorizationUrlRequest authorizationUrlRequest)
    {
        String state = RandomUtil.randomNumbers(5);
        String url = githubClient.getAuthServerUrl() +
                     "?client_id=" + githubClient.getClientId() +
                     "&redirect_uri=" + githubClient.getCallBackUrl() +
                     "&state=" + state;
        HashMap<String, String> stateMap = new HashMap<>();
        stateMap.put(STATE_KEY, state);
        stateMap.put(USER_REDIRECT_URI_KEY, authorizationUrlRequest.getRedirectUri());
        log.info("stateMap: {}", stateMap);
        log.info("authorizationUrlRequest: {}", authorizationUrlRequest);
        log.info("url: {}", url);
        redisUtils.setHashMap(RedisConstant.GITHUB_OAUTH, stateMap, authorizationUrlRequest.getSessionId());
        return url;
    }

    @Override
    public GithubCallbackResponse doCallback(GithubCallbackRequest callbackRequest)
    {
        HashMap<String, Object> authorizationMap = redisUtils.getHashMap(RedisConstant.GITHUB_OAUTH, String.class,
                Object.class,
                callbackRequest.getSessionId());
        if (authorizationMap == null)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "授权已过期或无效");
        }
        String stateKey = authorizationMap.get(STATE_KEY).toString().replaceAll("\"", "");
        log.info("stateKey:{}, callbackRequest.getState(): {}", stateKey, callbackRequest.getState());
        if (StringUtils.isBlank(stateKey) || !stateKey.equals(callbackRequest.getState()))
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "授权已过期或无效");
        }
        String accessToken = getAccessToken(callbackRequest.getCode());

        GithubCallbackResponse.GithubCallbackResponseBuilder responseBuilder = GithubCallbackResponse.builder();
        responseBuilder.redirectUri((String) authorizationMap.get(USER_REDIRECT_URI_KEY));
        responseBuilder.accessToken(accessToken);
        return responseBuilder.build();
    }

    @Override
    public GithubUserProfileDTO getUserProfile(GithubCallbackResponse callbackResponse)
    {
        GithubUserProfileDTO githubUserProfileDTO = fetchGitHubUserProfile(callbackResponse.getAccessToken());
        if (githubUserProfileDTO == null || githubUserProfileDTO.getMessage() != null)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取用户信息失败");
        }
        return githubUserProfileDTO;
    }

    @Override
    public OAuthResultResponse doAuth(Map<String, Object> paramMaps)
    {
        GithubCallbackRequest githubCallbackRequest = safetyConvertMapToObject(paramMaps, GithubCallbackRequest.class);
        GithubCallbackResponse githubCallbackResponse = doCallback(githubCallbackRequest);
        GithubUserProfileDTO userProfile = getUserProfile(githubCallbackResponse);
        if (userProfile == null)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取用户信息失败");
        }
        OAuthResultResponse.OAuthResultResponseBuilder resultBuilder = OAuthResultResponse.builder();
        resultBuilder.result(userProfile)
                     .success(true)
                     .redirectUrl(githubCallbackResponse.getRedirectUri())
                     .loginAdapter(getUserLoginByOAuthAdapter(userProfile));
        return resultBuilder.build();
    }

    private UserLoginByOAuthAdapter getUserLoginByOAuthAdapter(GithubUserProfileDTO userProfileDTO)
    {
        UserLoginByOAuthAdapter.UserLoginByOAuthAdapterBuilder adapterBuilder = UserLoginByOAuthAdapter.builder();
        User.UserBuilder userBuilder = User.builder();
        userBuilder.userEmail(userProfileDTO.getEmail())
                   .githubId(userProfileDTO.getId())
                   .userAvatar(userProfileDTO.getAvatarUrl())
                   .githubUserName(userProfileDTO.getLoginUserName())
                   .userGender(UserGenderEnum.UNKNOWN.getValue())
                   .userAccount(userProfileDTO.getLoginUserName())
                   .userRole(UserRoleEnum.USER.getValue())
                   .userName(userProfileDTO.getName());

        adapterBuilder.uniqueFieldName("githubId")
                      .uniqueFieldValue(userProfileDTO.getId())
                      .userInfo(userBuilder.build());
        return adapterBuilder.build();
    }

    private String getAccessToken(String code)
    {
        String host = githubClient.getAccessTokenUrl();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        Map<String, String> bodys = new HashMap<>();
        bodys.put("client_id", githubClient.getClientId());
        bodys.put("client_secret", githubClient.getClientSecret());
        bodys.put("code", code);
        bodys.put("grant_type", "authorization_code");

        try
        {
            HttpResponse response = HttpUtils.doPost(host, headers, null, bodys);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
            {
                // 错误相应
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证失败");
            }
            String responseStr = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            log.info("getAccessToken response: {}", responseStr);

            Map<String, Object> responseMap = JsonUtils.jsonToMap(responseStr);
            return (String) responseMap.get("access_token");
        }
        catch (IOException e)
        {
            log.error("Error while getting access token: ", e);
            return null;
        }
    }

    private GithubUserProfileDTO fetchGitHubUserProfile(String accessToken)
    {
        String host = githubClient.getFetchInfoUrl();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "token " + accessToken);
        headers.put("Accept", "application/json");

        try
        {
            HttpResponse response = HttpUtils.doGet(host, headers, null);

            String userInfo = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
            {
                // 错误相应
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证失败");
            }
            log.info("fetchGitHubUserProfile: {}", userInfo);

            return JsonUtils.jsonToObject(userInfo, GithubUserProfileDTO.class);
        }
        catch (IOException e)
        {
            log.error("Error while fetching user profile: ", e);
            return null;
        }
    }
}
