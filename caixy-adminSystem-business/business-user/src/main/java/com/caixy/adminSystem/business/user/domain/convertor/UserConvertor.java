package com.caixy.adminSystem.business.user.domain.convertor;

import com.caixy.adminSystem.business.user.domain.entity.User;
import com.caixy.adminSystem.common.api.common.BaseConvertor;
import com.caixy.adminSystem.common.api.user.vo.AboutMeVO;
import com.caixy.adminSystem.common.api.user.vo.LoginUserVO;
import com.caixy.adminSystem.common.api.user.vo.UserVO;
import com.caixy.adminSystem.common.base.constant.RegexPatternConstants;
import com.caixy.adminSystem.common.base.constant.UserRoleEnum;
import com.caixy.adminSystem.common.base.utils.RegexUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户实体mapstruct转换器
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.entity.convertor.UserConvertor
 * @since 2024/8/9 上午1:27
 */
@Mapper
public interface UserConvertor extends BaseConvertor<User>
{
    UserConvertor INSTANCE = Mappers.getMapper(UserConvertor.class);

    @Mapping(source = "userRole", target = "userRole", qualifiedByName = "roleStringToEnum")
    UserVO toVO(User user);

    @Mapping(source = "userRole", target = "userRole", qualifiedByName = "roleStringToEnum")
    List<UserVO> toVOList(List<User> userList);

    @Mapping(source = "userEmail", target = "userEmail", qualifiedByName = "encryptEmailText")
    LoginUserVO voToLoginVO(UserVO userVO);

    @Mapping(source = "userEmail", target = "userEmail", qualifiedByName = "encryptEmailText")
    @Mapping(source = "userRole", target = "userRole", qualifiedByName = "roleStringToEnum")
    LoginUserVO toLoginVO(User user);

    User voToEntity(UserVO userVO);

    AboutMeVO toAboutMeVO(User user);

    /**
     * 忽略id字段进行转换
     */
    @Mapping(target = "id", ignore = true)
    void copyAllPropertiesIgnoringId(UserVO source, @MappingTarget User target);

    /**
     * 所有字段进行转换
     */
    User copyAllProperties(User source);

    @AfterMapping
    default void copyIfSourceValueIsNotNull(User source, @MappingTarget User target)
    {
        copyFields(source, target, null, (sourceValue, targetValue) -> sourceValue != null);
    }

    /**
     * 如果源值与目标值不同，则复制字段，默认忽略id字段
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/8/9 上午1:39
     */
    @AfterMapping
    default void copyIfDifferent(User source, @MappingTarget User target)
    {
        copyFields(source, target, null, (sourceValue, targetValue) -> !sourceValue.equals(targetValue));
    }

    @Named("roleStringToEnum")
    default UserRoleEnum mapRole(String role)
    {
        return UserRoleEnum.getEnumByValue(role);
    }

    @Named("encryptEmailText")
    default String encryptEmailText(String email)
    {
        return RegexUtils.encryptText(email, RegexPatternConstants.EMAIL_ENCRYPT_REGEX,
                "$1****$2");
    }

    @Named("encryptPhoneText")
    default String encryptPhoneText(String phone)
    {
        return RegexUtils.encryptText(phone, RegexPatternConstants.PHONE_ENCRYPT_REGEX,
                "$1****$2");
    }
}
