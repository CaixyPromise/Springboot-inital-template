package com.caixy.adminSystem.manager.uploadManager;

import com.caixy.adminSystem.annotation.UploadMethodTarget;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.config.CosClientConfig;
import com.caixy.adminSystem.constant.FileConstant;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.manager.uploadManager.core.UploadFileMethodManager;
import com.caixy.adminSystem.model.dto.file.UploadFileDTO;
import com.caixy.adminSystem.model.enums.SaveFileMethodEnum;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.utils.IOUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cos 对象存储操作
 */
@Component
@AllArgsConstructor
@UploadMethodTarget(SaveFileMethodEnum.TENCENT_COS_SAVE)
@Slf4j
public class CosManager implements UploadFileMethodManager
{
    private final CosClientConfig cosClientConfig;

    private final COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key           唯一键
     * @param localFilePath 本地文件路径
     * @return
     */
    public PutObjectResult putObject(String key, String localFilePath)
    {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                new File(localFilePath));
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file)
    {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    public void deleteObject(String key)
    {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

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

    public Path doSave(UploadFileDTO uploadFileDTO)
    {
        MultipartFile multipartFile = uploadFileDTO.getMultipartFile();
        UploadFileDTO.FileInfo fileInfo = uploadFileDTO.getFileInfo();
        Path filepath = fileInfo.getFileAbsolutePathAndName();
        File file = null;
        try
        {
            // 上传文件
            file = File.createTempFile(filepath.toString(), null);
            multipartFile.transferTo(file);
            PutObjectResult result = putObject(filepath.toString(), file);
            // 本地存储
            // 返回可访问地址
            return Paths.get(FileConstant.COS_HOST, filepath.toString());
        }
        catch (Exception e)
        {
            log.error("file upload error, filepath = {}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
        finally
        {
            deleteFileOnLocal(file);
        }
    }

    @Override
    public Path saveFile(UploadFileDTO uploadFileDTO)
    {
        return doSave(uploadFileDTO);
    }

    @Override
    public void deleteFile(Path key)
    {
        cosClient.deleteObject(cosClientConfig.getBucket(), key.toString());
    }

    @Override
    public Resource getFile(Path key) throws IOException
    {
        GetObjectRequest objectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key.toString());
        COSObject fileObject = cosClient.getObject(objectRequest);
        byte[] bytes = IOUtils.toByteArray(fileObject.getObjectContent());
        return new ByteArrayResource(bytes);
    }
}
