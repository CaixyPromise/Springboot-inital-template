package com.caixy.adminSystem.infrastructure.file.services;


import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadContext;
import com.caixy.adminSystem.common.api.file.enums.FileActionBizEnum;
import com.caixy.adminSystem.infrastructure.file.strategy.FileActionStrategy;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @name: com.caixy.adminSystem.service.UploadFileService
 * @description: 文件上传下载服务
 * @author: CAIXYPROMISE
 * @date: 2024-05-21 21:52
 **/
public interface UploadFileService
{
    /**
     * 生成上传文件token，在判断文件存在时创建的
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/4/22 19:45
     */
    void setCheckFileCache(HashMap<String, Object> cacheMap, String token);

    /**
     * 解析token数组信息，并验证防重放攻击
     *
     * @param token 上传token
     * @param nonce 防重放标识
     * @param timestamp 时间戳
     * @param userId 用户ID
     * @return token对应的缓存信息
     */
    Map<String, Object> parasTokenInfoMap(String token, String nonce, Long timestamp, Long userId);

    /**
     * 清理token缓存（在请求成功后调用）
     *
     * @param token 上传token
     */
    void clearTokenCache(String token);

    Resource getFile(FileActionBizEnum fileActionBizEnum, Path filePath) throws IOException;

    void deleteFile(FileActionBizEnum fileActionBizEnum, Path filePath);

    void deleteFile(FileActionBizEnum fileActionBizEnum, Long userId, String filename);

    FileActionStrategy getFileActionService(FileActionBizEnum fileActionBizEnum);

    String handleFasterUpload(UploadContext uploadContext);

    void degradeToNormalUpload(Map<String, Object> cacheMap, String token);

    String handleUpload(UploadContext uploadContext);
}
