package com.caixy.adminSystem.model.enums;

import com.caixy.adminSystem.constant.FileTypeConstant;
import com.caixy.adminSystem.utils.SizeUtils;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文件上传业务类型枚举
 */
@Getter
public enum FileUploadBizEnum
{

    USER_AVATAR(
            "用户头像",
            "avatar",
            FileTypeConstant.AVATAR,
            SizeUtils.of(2, SizeUtils.SizeType.MB),
            new HashSet<>(Arrays.asList("jpeg", "jpg", "svg", "png", "webp")),
            "UserService"
    );

    /**
     * 用途说明
     */
    private final String text;
    /**
     * 文件的路由静态访问类型
     */
    private final String routePath;

    /**
     * 文件上传业务类型
     */
    private final String value;
    /**
     * 文件上传业务类型最大限制
     */
    private final SizeUtils.ByteSize maxSize;

    private final Set<String> fileSuffix;

    private final String handlerClassName;

    FileUploadBizEnum(String text,
                      String routePath,
                      String value,
                      SizeUtils.ByteSize maxSize,
                      Set<String> fileSuffix,
                      String handlerClassName
                       )
    {
        this.text = text;
        this.routePath = routePath;
        this.value = value;
        this.maxSize = maxSize;
        this.fileSuffix = fileSuffix;
        this.handlerClassName = handlerClassName;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues()
    {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static FileUploadBizEnum getEnumByValue(String value)
    {
        if (ObjectUtils.isEmpty(value))
        {
            return null;
        }
        for (FileUploadBizEnum anEnum : FileUploadBizEnum.values())
        {
            if (anEnum.value.equals(value))
            {
                return anEnum;
            }
        }
        return null;
    }

}
