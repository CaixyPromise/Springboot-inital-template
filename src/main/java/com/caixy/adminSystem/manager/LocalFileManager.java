package com.caixy.adminSystem.manager;

import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.config.LocalFileConfig;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.model.dto.file.UploadFileConfig;
import com.caixy.adminSystem.model.enums.FileUploadBizEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 本地文件管理器
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.LocalFileManager
 * @since 2024-05-21 20:13
 **/
@Component
@AllArgsConstructor
@Slf4j
public class LocalFileManager
{
    private final LocalFileConfig localFileConfig;

    public String saveFile(MultipartFile multipartFile, UploadFileConfig.FileInfo fileInfo)
    {
        // 文件目录：根据业务、用户来划分保存文件位置
        String filePath = fileInfo.getFilePath();
        String filename = fileInfo.getFilename();
        FileUploadBizEnum fileUploadBizEnum = fileInfo.getFileUploadBizEnum();
        Long userId = fileInfo.getUserId();
        // 创建文件目录
        File directory = new File(localFileConfig.getRootLocation().toString(), filePath);
        if (!directory.exists())
        {
            boolean mkdirs = directory.mkdirs();
            if (!mkdirs)
            {
                log.error("create directory error, directory = {}", directory.getPath());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
            }
        }
        File file = new File(directory, filename);
        try
        {
            multipartFile.transferTo(file);
            // <saveLocation>/<getValue>/<userId>/<filename>
            return localFileConfig.getLocation() + "/" + fileUploadBizEnum.getValue() + "/" + userId +
                    "/" + filename;
        }
        catch (Exception e)
        {
            log.error("file upload error, filepath = {}", file.getPath(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }
}
