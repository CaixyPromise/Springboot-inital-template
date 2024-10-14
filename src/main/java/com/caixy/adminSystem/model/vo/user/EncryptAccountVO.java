package com.caixy.adminSystem.model.vo.user;

import com.caixy.adminSystem.common.BaseSerializablePayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 加密后的账号信息VO
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.vo.user.EncryptAccountVO
 * @since 2024/10/14 22:18
 */
@Getter
@Setter
@AllArgsConstructor
public class EncryptAccountVO extends BaseSerializablePayload
{
    /**
     * 加密后的手机
     */
    private String phone;
    /**
     * 加密后的邮箱
     */
    private String email;
}
