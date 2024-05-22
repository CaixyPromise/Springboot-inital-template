package com.caixy.adminSystem.service;

import com.caixy.adminSystem.model.dto.file.UploadFileConfig;

/**
 * @name: com.caixy.adminSystem.service.FileUploadActionService
 * @description: 文件上传操作接口类
 * @author: CAIXYPROMISE
 * @date: 2024-05-22 16:51
 **/
public interface FileUploadActionService
{
    Boolean doAfterUpload(UploadFileConfig uploadFileConfig, String savePath);
}
