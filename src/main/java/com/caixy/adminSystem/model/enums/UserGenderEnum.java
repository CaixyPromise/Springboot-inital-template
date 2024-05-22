package com.caixy.adminSystem.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户性别枚举
 */
@Getter
public enum UserGenderEnum
{
    UNKNOWN("未知", 0),
    MALE("男", 1),
    FEMALE("被封号", 2);

    private final String text;

    private final Integer value;

    UserGenderEnum(String text, Integer value)
    {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues()
    {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static UserGenderEnum getEnumByValue(Integer value)
    {
        if (value == null)
        {
            return null;
        }
        for (UserGenderEnum anEnum : UserGenderEnum.values())
        {
            if (anEnum.value.equals(value))
            {
                return anEnum;
            }
        }
        return null;
    }
}
