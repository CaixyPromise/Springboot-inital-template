package com.caixy.adminSystem.service;

import com.caixy.adminSystem.model.dto.file.UploadFileConfig;
import com.caixy.adminSystem.model.enums.FileUploadBizEnum;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @name: com.caixy.adminSystem.service.FileService
 * @description: 文件上传下载服务
 * @author: CAIXYPROMISE
 * @date: 2024-05-21 21:52
 **/
public interface FileService
{
    /**
     * 上传文件到COS
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/5/21 下午10:32
     */
    String saveFileToCos(UploadFileConfig uploadFileConfig);

    /**
     * 本地存储文件
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/5/21 下午10:32
     */
    String saveFileToLocal(UploadFileConfig uploadFileConfig);

    void deleteFileOnCos(String filepath);

    void deleteFileOnLocal(File file);
}
