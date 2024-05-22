package com.caixy.adminSystem.service.impl;

import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.constant.FileConstant;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.manager.CosManager;
import com.caixy.adminSystem.manager.LocalFileManager;
import com.caixy.adminSystem.model.dto.file.UploadFileConfig;
import com.caixy.adminSystem.service.FileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件服务实现类
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.service.impl.FileServiceImpl
 * @since 2024-05-21 21:55
 **/
@Service
@Slf4j
@AllArgsConstructor
public class FileServiceImpl implements FileService
{
    private final CosManager cosManager;

    private final LocalFileManager localFileManager;

    /**
     * 上传文件到COS
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/5/21 下午10:32
     */
    @Override
    public String saveFileToCos(UploadFileConfig uploadFileConfig)
    {
        MultipartFile multipartFile = uploadFileConfig.getMultipartFile();
        UploadFileConfig.FileInfo fileInfo = uploadFileConfig.convertFileInfo(false);
        uploadFileConfig.setFileInfo(fileInfo);
        String filepath = fileInfo.getFilePath();
        File file = null;
        try
        {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 本地存储
            // 返回可访问地址
            return FileConstant.COS_HOST + filepath;
        }
        catch (Exception e)
        {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
        finally
        {
            deleteFileOnLocal(file);
        }
    }

    /**
     * 本地存储文件
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/5/21 下午10:32
     */
    @Override
    public String saveFileToLocal(UploadFileConfig uploadFileConfig)
    {
        UploadFileConfig.FileInfo fileInfo = uploadFileConfig.convertFileInfo(true);
        uploadFileConfig.setFileInfo(fileInfo);
        return localFileManager.saveFile(uploadFileConfig.getMultipartFile(), fileInfo);
    }

    @Override
    public void deleteFileOnCos(String filepath)
    {
        cosManager.deleteObject(filepath);
    }

    @Override
    public void deleteFileOnLocal(File file)
    {
        if (file != null && file.exists())
        {
            // 删除临时文件
            boolean delete = file.delete();
            if (!delete)
            {
                log.error("file delete error, filepath = {}", file.getAbsolutePath());
            }
        }
    }
}
