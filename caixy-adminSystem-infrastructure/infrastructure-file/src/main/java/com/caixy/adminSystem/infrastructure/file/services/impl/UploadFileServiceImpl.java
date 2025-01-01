package com.caixy.adminSystem.infrastructure.file.services.impl;


import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.response.ErrorCode;
import com.caixy.adminSystem.common.base.utils.SpringContextUtils;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadFileDTO;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadFileRequest;
import com.caixy.adminSystem.infrastructure.file.domain.enums.FileActionBizEnum;
import com.caixy.adminSystem.infrastructure.file.domain.enums.SaveFileMethodEnum;
import com.caixy.adminSystem.infrastructure.file.exception.FileUploadActionException;
import com.caixy.adminSystem.infrastructure.file.manager.annotation.FileUploadActionTarget;
import com.caixy.adminSystem.infrastructure.file.manager.annotation.UploadMethodTarget;
import com.caixy.adminSystem.infrastructure.file.services.UploadFileService;
import com.caixy.adminSystem.infrastructure.file.strategy.FileActionStrategy;
import com.caixy.adminSystem.infrastructure.file.strategy.UploadFileMethodStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    @Resource
    private List<FileActionStrategy> fileActionStrategy;

    private ConcurrentHashMap<FileActionBizEnum, FileActionStrategy> serviceCache;

    @PostConstruct
    public void initActionService()
    {
        serviceCache =
                SpringContextUtils.getServiceFromAnnotation(fileActionStrategy, FileUploadActionTarget.class, "value");
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
        uploadFileDTO.getFileInfo().setFileURL(uploadFileMethodStrategy.buildFileURL(uploadFileDTO.getUserId(), uploadFileDTO.getFileInfo().getFileInnerName()));
        return uploadFileMethodStrategy.saveFile(uploadFileDTO);
    }



    /**
     * 获取业务文件上传处理器
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/6/11 下午8:00
     */
    @Override
    public FileActionStrategy getFileActionService(FileActionBizEnum fileActionBizEnum)
    {
        FileActionStrategy actionService = serviceCache.get(fileActionBizEnum);
        if (actionService == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "暂无该文件对应业务的操作");
        }
        return actionService;
    }
    /**
     * 处理上传逻辑
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/19 上午1:27
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleUpload(UploadFileRequest uploadFileRequest, FileActionBizEnum uploadBizEnum,
                               SaveFileMethodEnum saveFileMethod, UploadFileDTO uploadFileDTO,
                               HttpServletRequest request)
    {
        Path savePath = null;

        try
        {
            // 获取文件处理类，如果找不到就会直接报错
            FileActionStrategy actionService = getFileActionService(uploadBizEnum);
            boolean doVerifyFileToken = doBeforeFileUploadAction(actionService, uploadFileDTO, uploadFileRequest);
            if (!doVerifyFileToken)
            {
                log.error("{}-验证token：文件上传失败，文件信息：{}, 上传用户Id: {}", saveFileMethod.getDesc(),
                        uploadFileDTO.getFileInfo(),
                        uploadFileDTO.getUserId());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
            }
            savePath = saveFile(uploadFileDTO);
            boolean doAfterFileUpload =
                    doAfterFileUploadAction(actionService, uploadFileDTO, savePath, uploadFileRequest, request);
            if (!doAfterFileUpload)
            {
                log.error("{}：文件上传成功，文件路径：{}，但后续处理失败", saveFileMethod.getDesc(), savePath);
                deleteFile(uploadFileDTO.getFileActionBizEnum(), savePath);

                log.error("{}：文件上传成功，文件路径：{}，后处理失败后，成功删除文件", saveFileMethod.getDesc(), savePath);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传成功，但后续处理失败");
            }
            log.info("{}：文件上传成功，文件路径：{}", saveFileMethod.getDesc(), savePath);
            return uploadFileDTO.getFileInfo().getFileURL();
        }
        catch (FileUploadActionException | BusinessException | IOException e)
        {
            log.error("{}: 文件上传失败，错误信息: {}", saveFileMethod.getDesc(), e.getMessage());
            // 如果 savePath 不为空，则意味着文件已经上传成功，需要删除它
            if (savePath != null)
            {
                deleteFile(uploadBizEnum, savePath);
                log.info("{}：文件上传失败，删除文件成功，文件路径：{}", saveFileMethod.getDesc(), savePath);
            }
            // 抛出业务异常，以触发事务回滚
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    private boolean doAfterFileUploadAction(FileActionStrategy actionService, UploadFileDTO uploadFileDTO, Path savePath, UploadFileRequest uploadFileRequest, HttpServletRequest request) throws IOException
    {
        return actionService.doAfterUploadAction(uploadFileDTO, savePath, uploadFileRequest, request);
    }

    private boolean doBeforeFileUploadAction(FileActionStrategy actionService, UploadFileDTO uploadFileDTO
            , UploadFileRequest uploadFileRequest)
    {
        return actionService.doBeforeUploadAction(uploadFileDTO, uploadFileRequest);
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
