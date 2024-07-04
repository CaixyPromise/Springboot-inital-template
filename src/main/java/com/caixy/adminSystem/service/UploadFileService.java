package com.caixy.adminSystem.service;

import com.caixy.adminSystem.model.dto.file.UploadFileDTO;
import com.caixy.adminSystem.model.enums.FileActionBizEnum;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @name: com.caixy.adminSystem.service.UploadFileService
 * @description: 文件上传下载服务
 * @author: CAIXYPROMISE
 * @date: 2024-05-21 21:52
 **/
public interface UploadFileService
{
    org.springframework.core.io.Resource getFile(FileActionBizEnum fileActionBizEnum, Path filePath) throws IOException;

    void deleteFile(FileActionBizEnum fileActionBizEnum, Path filePath);

    void deleteFile(FileActionBizEnum fileActionBizEnum, Long userId, String filename);

    Path saveFile(UploadFileDTO uploadFileDTO) throws IOException;
}
