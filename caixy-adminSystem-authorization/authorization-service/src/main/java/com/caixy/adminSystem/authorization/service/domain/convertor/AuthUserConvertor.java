package com.caixy.adminSystem.authorization.service.domain.convertor;

import com.caixy.adminSystem.common.api.user.vo.LoginUserVO;
import com.caixy.adminSystem.common.api.user.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户信息转换器
 *
 * @Author CAIXYPROMISE
 * @since 2024/12/31 0:05
 */
@Mapper
public interface AuthUserConvertor
{
    AuthUserConvertor INSTANCE = Mappers.getMapper(AuthUserConvertor.class);
    LoginUserVO voToLoginVO(UserVO user);
}
