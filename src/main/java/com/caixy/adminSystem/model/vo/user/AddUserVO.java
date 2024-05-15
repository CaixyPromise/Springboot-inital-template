package com.caixy.adminSystem.model.vo.user;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理生成用户信息返回的VO
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.vo.user.AddUserVO
 * @since 2024-05-15 11:50
 **/
@Data
@Builder
public class AddUserVO implements Serializable
{
    private Long id;
    private String userName;
    private String userAccount;
    private String userPassword;
    private static final long serializableVersionUID = 1L;
}
