package com.caixy.adminSystem.business.wx.facade;


import com.caixy.adminSystem.business.wx.config.WxOpenConfig;
import com.caixy.adminSystem.common.api.wx.dto.WxOAuth2LoginDTO;
import com.caixy.adminSystem.common.api.wx.service.WxFacadeService;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.stereotype.Service;

/**
 * 微信门面服务类实现
 *
 * @Author CAIXYPROMISE
 * @since 2024/12/31 0:15
 */
@Service
@RequiredArgsConstructor
public class WxFacadeServiceImpl implements WxFacadeService
{
    private final WxOpenConfig wxOpenConfig;

    @Override
    public WxOAuth2LoginDTO getUserInfoByWxOpen(String code)
    {
        WxOAuth2AccessToken accessToken;
        try {
            WxMpService wxService = wxOpenConfig.getWxMpService();
            accessToken = wxService.getOAuth2Service().getAccessToken(code);
            WxOAuth2UserInfo userInfo = wxService.getOAuth2Service().getUserInfo(accessToken, code);
            return WxOAuth2LoginDTO.builder()
                                   .nickName(userInfo.getNickname())
                                   .unionId(userInfo.getUnionId())
                                   .mpOpenId(userInfo.getOpenid())
                                   .userAvatar(userInfo.getHeadImgUrl())
                                   .build();
        }
        catch (Exception e) {
            return null;
        }
    }
}
