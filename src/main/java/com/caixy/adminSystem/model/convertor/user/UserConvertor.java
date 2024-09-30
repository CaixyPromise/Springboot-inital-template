package com.caixy.adminSystem.model.convertor.user;


import com.caixy.adminSystem.model.convertor.BaseConvertor;
import com.caixy.adminSystem.model.entity.User;
import com.caixy.adminSystem.model.enums.UserRoleEnum;
import com.caixy.adminSystem.model.vo.user.LoginUserVO;
import com.caixy.adminSystem.model.vo.user.UserVO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;


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
    void toVO(User user, @MappingTarget UserVO userVO);

    @Mapping(source = "userRole", target = "userRole", qualifiedByName = "roleStringToEnum")
    void toLoginVO(User user, @MappingTarget LoginUserVO userVO);

    void voToLoginVO(UserVO userVO, @MappingTarget LoginUserVO loginUserVO);


    /**
     * 忽略id字段进行转换
     */
    @Mapping(target = "id", ignore = true)
    void copyAllPropertiesIgnoringId(User source, @MappingTarget User target);

    /**
     * 所有字段进行转换
     */

    void copyAllProperties(User source, @MappingTarget User target);

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
}
