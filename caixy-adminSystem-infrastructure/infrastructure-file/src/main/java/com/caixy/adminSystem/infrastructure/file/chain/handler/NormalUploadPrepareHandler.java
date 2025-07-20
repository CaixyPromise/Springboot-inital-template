package com.caixy.adminSystem.infrastructure.file.chain.handler;

import cn.hutool.crypto.digest.DigestUtil;
import com.caixy.adminSystem.common.base.chain.ChainContext;
import com.caixy.adminSystem.common.base.chain.ChainHandler;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.exception.ErrorCode;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadContext;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadFileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * 上传业务流程前处理：秒传检查&普通上传校验 & 读取 Redis token 信息
 *
 * @Author CAIXYPROMISE
 * @since 2025/5/27 1:22
 */
public class NormalUploadPrepareHandler implements ChainHandler<UploadContext, String>
{

    @Override
    public void handle(ChainContext<UploadContext, String> context)
    {
        UploadContext ctx = context.getData();
        UploadFileDTO uploadFileDTO = ctx.getUploadFileDTO();
        Map<String, Object> cacheMap = ctx.getCacheMap();
        if (cacheMap == null || cacheMap.isEmpty())
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传 token 无效");
        }
        // 普通上传时，再次校验文件 SHA256 & 大小
        MultipartFile file = uploadFileDTO.getMultipartFile();
        try (InputStream in = file.getInputStream())
        {
            String calc = DigestUtil.sha256Hex(in);
            if (!calc.equals(uploadFileDTO.getSha256()) || file.getSize() != uploadFileDTO.getFileSize())
            {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件 SHA256 或 大小 校验失败");
            }
        }
        catch (Exception e)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件校验异常: " + e.getMessage());
        }
    }
}
