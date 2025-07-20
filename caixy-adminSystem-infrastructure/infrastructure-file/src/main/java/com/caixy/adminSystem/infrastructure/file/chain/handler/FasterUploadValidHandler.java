package com.caixy.adminSystem.infrastructure.file.chain.handler;

import com.caixy.adminSystem.common.api.file.dto.FileHmacInfo;
import com.caixy.adminSystem.common.api.file.dto.FileInfoDTO;
import com.caixy.adminSystem.common.base.chain.ChainContext;
import com.caixy.adminSystem.common.base.chain.ChainHandler;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.exception.ErrorCode;
import com.caixy.adminSystem.common.base.utils.FileUtils;
import com.caixy.adminSystem.common.base.utils.JsonUtils;
import com.caixy.adminSystem.common.base.utils.MapUtils;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadContext;
import com.caixy.adminSystem.infrastructure.file.strategy.UploadFileMethodStrategy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 秒传文件校验文件逻辑
 *
 * @Author CAIXYPROMISE
 * @since 2025/6/23 18:30
 */
public class FasterUploadValidHandler implements ChainHandler<UploadContext, String>
{
    @Override
    public void handle(ChainContext<UploadContext, String> context)
    {
        UploadContext ctx = context.getData();
        // 1. 取出token缓存的校验信息
        Map<String, Object> fileInfoMap = ctx.getCacheMap();
        if (MapUtils.isEmpty(fileInfoMap))
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "缺少校验信息");
        }
        FileHmacInfo encryptInfo = JsonUtils.jsonToObject(fileInfoMap.getOrDefault("hmacBody", "{}").toString(), FileHmacInfo.class);
        if (encryptInfo == null)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "缺少校验信息");
        }
        // 2. 读取文件片段
        FileInfoDTO fileInfo = ctx.getFileInfo();
        Path filePath = Paths.get(fileInfo.getStoragePath());
        UploadFileMethodStrategy methodStrategy = ctx.getUploadFileMethodStrategy();
        try
        {
            // 获取文件切片信息
            byte[] slice = methodStrategy.readSlice(filePath, encryptInfo.getOffset(), encryptInfo.getLength());
            // 3. 拼接前端的dataToSign: nonce + timestamp + challenge + 文件片段内容
            String nonce = encryptInfo.getNonce();
            Long timestamp = encryptInfo.getTimestamp();
            String challenge = encryptInfo.getChallenge();

            byte[] nonceBytes = nonce != null ? nonce.getBytes(StandardCharsets.UTF_8) : new byte[0];
            byte[] timestampBytes = timestamp != null ? String.valueOf(timestamp).getBytes(StandardCharsets.UTF_8) : new byte[0];
            byte[] challengeBytes = challenge != null ? challenge.getBytes(StandardCharsets.UTF_8) : new byte[0];

            // 拼接所有字节
            ByteBuffer buffer = ByteBuffer.allocate(
                nonceBytes.length + timestampBytes.length + challengeBytes.length + slice.length
            );
            buffer.put(nonceBytes);
            buffer.put(timestampBytes);
            buffer.put(challengeBytes);
            buffer.put(slice);
            byte[] dataToSign = buffer.array();

            // 4. 用 challenge 作为 key 做 HMAC
            String calcHmac = FileUtils.hmacSha256File(dataToSign, challenge);
            // 5. 校验
            if (!calcHmac.equals(ctx.getUploadFileRequest().getSignature()))
            {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件校验失败");
            }
        }
        catch (IOException e)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件校验失败");
        }
    }
}