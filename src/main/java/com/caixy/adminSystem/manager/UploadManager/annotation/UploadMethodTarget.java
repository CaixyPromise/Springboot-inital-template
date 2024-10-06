package com.caixy.adminSystem.manager.UploadManager.annotation;

import com.caixy.adminSystem.model.enums.SaveFileMethodEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @name: com.caixy.adminSystem.manager.UploadManager.annotation.UploadMethodTarget
 * @description: 上传服务标注
 * @author: CAIXYPROMISE
 * @date: 2024-06-21 20:41
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UploadMethodTarget
{
    SaveFileMethodEnum value();
}
