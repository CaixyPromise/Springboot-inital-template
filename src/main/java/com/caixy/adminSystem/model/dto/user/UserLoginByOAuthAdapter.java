package com.caixy.adminSystem.model.dto.user;

import com.caixy.adminSystem.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户Oauth登录的适配器接口
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.dto.user.UserLoginByOAuthAdapter
 * @since 2024/8/7 上午2:17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginByOAuthAdapter implements Serializable
{
    /**
     * 用户唯一标识字段名
     */
    private String uniqueFieldName;
    
    /**
     * 用户唯一标识字段值
     */
    private Object uniqueFieldValue;
    
    /**
     * 用户信息实体
     */
    private User userInfo;
    
    private static final long serialVersionUID = 1L;
}
