package com.caixy.adminSystem.service.impl;

import com.caixy.adminSystem.manager.UploadManager.annotation.UploadMethodTarget;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.strategy.UploadFileMethodStrategy;
import com.caixy.adminSystem.model.dto.file.UploadFileDTO;
import com.caixy.adminSystem.model.enums.FileActionBizEnum;
import com.caixy.adminSystem.model.enums.SaveFileMethodEnum;
import com.caixy.adminSystem.service.UploadFileService;
import com.caixy.adminSystem.utils.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * 文件服务实现类
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.service.impl.UploadFileServiceImpl
 * @since 2024-05-21 21:55
 **/
@Service
@Slf4j
public class UploadFileServiceImpl implements UploadFileService
{
    @Resource
    private List<UploadFileMethodStrategy> uploadFileMethodStrategies;

    private Map<SaveFileMethodEnum, UploadFileMethodStrategy> uploadFileMethodMap;

    @PostConstruct
    public void initUploadFileMethods()
    {
        uploadFileMethodMap =
                SpringContextUtils.getServiceFromAnnotation(uploadFileMethodStrategies, UploadMethodTarget.class, "value");
    }

    @Override
    public org.springframework.core.io.Resource getFile(FileActionBizEnum fileActionBizEnum, Path filePath) throws IOException
    {
        UploadFileMethodStrategy uploadFileMethodStrategy = safetyGetUploadFileMethod(fileActionBizEnum.getSaveFileMethod());
        return uploadFileMethodStrategy.getFile(filePath);
    }

    @Override
    public void deleteFile(FileActionBizEnum fileActionBizEnum, Path filePath)
    {
        UploadFileMethodStrategy uploadFileMethodStrategy = safetyGetUploadFileMethod(fileActionBizEnum.getSaveFileMethod());
        try
        {
            uploadFileMethodStrategy.deleteFile(filePath);
        }
        catch (IOException e)
        {
            log.error("file delete error, filepath = {}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除文件失败: " + e);
        }
    }


    @Override
    public void deleteFile(FileActionBizEnum fileActionBizEnum, Long userId, String filename)
    {
        Path filePath = fileActionBizEnum.buildFileAbsolutePathAndName(userId, filename);
        UploadFileMethodStrategy uploadFileMethodStrategy = safetyGetUploadFileMethod(fileActionBizEnum.getSaveFileMethod());
        try
        {
            uploadFileMethodStrategy.deleteFile(filePath);
        }
        catch (IOException e)
        {
            log.error("file delete error, filepath = {}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除文件失败: " + e);
        }
    }

    @Override
    public Path saveFile(UploadFileDTO uploadFileDTO) throws IOException
    {
        FileActionBizEnum fileActionBizEnum = uploadFileDTO.getFileActionBizEnum();
        UploadFileMethodStrategy uploadFileMethodStrategy = safetyGetUploadFileMethod(fileActionBizEnum.getSaveFileMethod());
        // 把上传服务处理类暴露给 后处理操作 ，例如需要删除前置文件信息等
        uploadFileDTO.setUploadManager(uploadFileMethodStrategy);
        return uploadFileMethodStrategy.saveFile(uploadFileDTO);
    }

    private UploadFileMethodStrategy safetyGetUploadFileMethod(SaveFileMethodEnum saveFileMethodEnum)
    {
        UploadFileMethodStrategy uploadFileMethodStrategy = uploadFileMethodMap.get(saveFileMethodEnum);
        if (uploadFileMethodStrategy == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的文件操作方式");
        }
        return uploadFileMethodStrategy;
    }
}
