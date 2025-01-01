package com.caixy.adminSystem.common.api.wx.service;

import com.caixy.adminSystem.common.api.user.vo.LoginUserVO;
import com.caixy.adminSystem.common.api.wx.dto.WxOAuth2LoginDTO;

/**
 * 微信门面调用接口
 *
 * @Author CAIXYPROMISE
 * @since 2024/12/31 0:14
 */
public interface WxFacadeService
{
    /**
     * 公众号登录获取用户信息
     *
     * @param code
     * @return
     */
    WxOAuth2LoginDTO getUserInfoByWxOpen(String code);
}
