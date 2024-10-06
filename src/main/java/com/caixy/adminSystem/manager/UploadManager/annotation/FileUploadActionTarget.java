package com.caixy.adminSystem.manager.UploadManager.annotation;

import com.caixy.adminSystem.model.enums.FileActionBizEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 文件上传业务处理器
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.UploadManager.annotation.FileUploadActionTarget
 * @since 2024-07-18 01:39
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FileUploadActionTarget
{
    FileActionBizEnum value();
}