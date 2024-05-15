package com.caixy.adminSystem.model.dto.user;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 修改密码请求
 *
 * @name: com.caixy.adminSystem.model.dto.user.UserModifyPasswordRequest
 * @author: CAIXYPROMISE
 * @since: 2024-04-14 20:42
 **/
@Data
public class UserModifyPasswordRequest implements Serializable
{
    /**
     * 旧密码
     */
    @NotNull
    @Min(8)
    @Max(20)
    private String oldPassword;


    /**
     * 新密码
     */
    @NotNull
    @Min(8)
    @Max(20)
    private String newPassword;

    /**
     * 确定密码
     */
    @NotNull
    @Min(8)
    @Max(20)
    private String confirmPassword;

    private static final long serialVersionUID = 1L;
}
