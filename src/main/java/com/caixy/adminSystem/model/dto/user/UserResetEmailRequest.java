package com.caixy.adminSystem.model.dto.user;

import com.caixy.adminSystem.common.BaseSerializablePayload;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 重置邮箱请求
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.dto.user.UserResetEmailRequest
 * @since 2024/10/10 下午9:03
 */
@Getter
@Setter
public class UserResetEmailRequest extends BaseSerializablePayload
{
    /**
     * 邮箱验证码
     */
    @NotNull
    @NotEmpty
    private String code;

    /**
     * 重置密码
     */
    @NotNull
    @NotEmpty
    private String password;
}
