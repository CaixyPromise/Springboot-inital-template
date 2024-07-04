package com.caixy.adminSystem.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @name: com.caixy.adminSystem.model.enums.SaveFileMethodEnum
 * @description: 保存文件方式
 * @author: CAIXYPROMISE
 * @date: 2024-06-21 16:56
 **/
@Getter
@AllArgsConstructor
public enum SaveFileMethodEnum
{

    /**
     * 本地保存
     */
    LOCAL_SAVE(0, "本地保存", false),
    /**
     * 腾讯云COS保存
     */
    TENCENT_COS_SAVE(2, "腾讯云COS保存", false),

    ;
    /**
     * 编码
     */
    private final Integer code;
    /**
     * 描述
     */
    private final String desc;

    /**
     * 是否是本地存储
     */
    private final Boolean isLocal;

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues()
    {
        return Arrays.stream(values()).map(item -> item.code).collect(Collectors.toList());
    }


    /**
     * 根据code获取枚举
     */
    public static SaveFileMethodEnum getEnumByCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (SaveFileMethodEnum value : SaveFileMethodEnum.values())
        {
            if (value.getCode().equals(code))
            {
                return value;
            }
        }
        return null;
    }

}
