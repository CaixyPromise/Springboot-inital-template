package com.caixy.adminSystem.infrastructure.file.services.impl;

import com.caixy.adminSystem.common.api.file.dto.FileInfoDTO;
import com.caixy.adminSystem.common.api.file.enums.SaveFileMethodEnum;
import com.caixy.adminSystem.common.api.file.facade.FileActionHelper;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.exception.ErrorCode;
import com.caixy.adminSystem.common.base.utils.SpringContextUtils;
import com.caixy.adminSystem.infrastructure.file.chain.UploadChainFactory;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadContext;
import com.caixy.adminSystem.common.api.file.enums.FileActionBizEnum;
import com.caixy.adminSystem.infrastructure.file.domain.enums.FileRedisBizKey;
import com.caixy.adminSystem.infrastructure.file.manager.annotation.FileUploadActionTarget;
import com.caixy.adminSystem.infrastructure.file.manager.annotation.UploadMethodTarget;
import com.caixy.adminSystem.infrastructure.file.services.UploadFileService;
import com.caixy.adminSystem.infrastructure.file.strategy.FileActionStrategy;
import com.caixy.adminSystem.infrastructure.file.strategy.UploadFileMethodStrategy;
import com.caixy.adminSystem.infrastucture.cache.redis.RedisManager;
import com.caixy.adminSystem.infrastucture.cache.redis.replayAttack.ReplayAttackManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
@RequiredArgsConstructor
public class UploadFileServiceImpl implements UploadFileService
{
    private final List<UploadFileMethodStrategy> uploadFileMethodStrategies;
    private final RedisManager redisManager;
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final FileActionHelper fileActionHelper;
    private final UploadChainFactory uploadChainFactory;

    private Map<SaveFileMethodEnum, UploadFileMethodStrategy> uploadFileMethodMap;

    private final List<FileActionStrategy> fileActionStrategy;

    private final ReplayAttackManager replayAttackManager;

    private ConcurrentHashMap<FileActionBizEnum, FileActionStrategy> serviceCache;

    @PostConstruct
    public void initActionService() {
        serviceCache = SpringContextUtils.getServiceFromAnnotation(fileActionStrategy, FileUploadActionTarget.class,
                                                                   "value");
        uploadFileMethodMap = SpringContextUtils.getServiceFromAnnotation(uploadFileMethodStrategies,
                                                                          UploadMethodTarget.class, "value");
    }

    @Override
    public void setCheckFileCache(HashMap<String, Object> cacheMap, String token)
    {
        redisManager.setHashMap(FileRedisBizKey.UPLOAD_EXIST_TOKEN, cacheMap, token);
    }

    /**
     * 解析token数组信息，并验证防重放攻击
     *
     * @author CAIXYPROMISE
     * @version 2.0 添加防重放验证，改为getHashMap而不是getHashMapThenRemove
     * @version 2025/1/27 16:24
     */
    @Override
    public Map<String, Object> parasTokenInfoMap(String token, String nonce, Long timestamp, Long userId) {
        // 1. 验证防重放攻击
        replayAttackManager.validateReplayAttack(nonce, timestamp, userId);

        // 2. 获取token对应的缓存信息（不删除）
        Map<String, Object> fileInfoMap = redisManager.getHashMap(FileRedisBizKey.UPLOAD_EXIST_TOKEN, token);
        if (fileInfoMap == null || fileInfoMap.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传token无效");
        }
        return fileInfoMap;
    }

    /**
     * 清理token缓存（在请求成功后调用）
     *
     * @param token 上传token
     */
    public void clearTokenCache(String token) {
        redisManager.delete(FileRedisBizKey.UPLOAD_EXIST_TOKEN, token);
    }

    @Override
    public Resource getFile(FileActionBizEnum fileActionBizEnum, Path filePath) throws IOException
    {
        UploadFileMethodStrategy uploadFileMethodStrategy =
                safetyGetUploadFileMethod(fileActionBizEnum.getSaveFileMethod());
        return uploadFileMethodStrategy.getFile(filePath);
    }

    @Override
    public void deleteFile(FileActionBizEnum fileActionBizEnum, Path filePath)
    {
        UploadFileMethodStrategy uploadFileMethodStrategy =
                safetyGetUploadFileMethod(fileActionBizEnum.getSaveFileMethod());
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
        UploadFileMethodStrategy uploadFileMethodStrategy =
                safetyGetUploadFileMethod(fileActionBizEnum.getSaveFileMethod());
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

    @Override
    public String handleFasterUpload(UploadContext uploadContext)
    {
        uploadContext.setFileActionHelper(fileActionHelper);
        // 上传方法从FileInfo的Type来
        Map<String, Object> cacheMap = uploadContext.getCacheMap();
        Object fileInfoStr = Optional.ofNullable(cacheMap.get("fileInfo"))
                                     .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "文件信息不存在"));
        FileInfoDTO fileInfo = objectMapper.convertValue(fileInfoStr, FileInfoDTO.class);
        if (fileInfo == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件信息不存在");
        }
        Integer storageType = fileInfo.getStorageType();
        uploadContext.setFileInfo(fileInfo);
        SaveFileMethodEnum fileMethodEnum = SaveFileMethodEnum.getEnumByCode(storageType);
        if (fileMethodEnum == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的文件操作方式");
        }
        FileActionBizEnum fileActionEnum = uploadContext.getUploadFileRequest().getBiz();
        uploadContext.setUploadFileMethodStrategy(uploadFileMethodMap.get(fileMethodEnum));
        uploadContext.setFileActionStrategy(getFileActionService(fileActionEnum));
        return uploadChainFactory.doFasterUpload(uploadContext);
    }

    @Override
    public void degradeToNormalUpload(Map<String, Object> cacheMap, String token)
    {
        cacheMap.put("degrade", true);
        redisManager.setHashMap(FileRedisBizKey.UPLOAD_EXIST_TOKEN, cacheMap, token);
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
    public String handleUpload(UploadContext uploadContext)
    {
        uploadContext.setFileActionHelper(fileActionHelper);
        uploadContext.setUploadFileMethodStrategy(uploadFileMethodMap.get(uploadContext.getUploadFileDTO()
                                                                                       .getFileActionBizEnum()
                                                                                       .getSaveFileMethod()));
        uploadContext.setFileActionStrategy(getFileActionService(uploadContext.getUploadFileDTO()
                                                                              .getFileActionBizEnum()));
        return uploadChainFactory.doNormalUpload(uploadContext);
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
