package com.caixy.adminSystem.model.entity.convertor;

import com.caixy.adminSystem.model.entity.User;
import com.caixy.adminSystem.utils.BaseConvertor;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Set;


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

    /**
     * 忽略id字段进行转换
     */
    @Mapping(target = "id", ignore = true)
    // 如果需要忽略特定字段
    User copyAllPropertiesIgnoringId(User source, @MappingTarget User target);

    /**
     * 所有字段进行转换
     */

    User copyAllProperties(User source, @MappingTarget User target);
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


}
