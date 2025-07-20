package com.caixy.adminSystem.infrastructure.file.chain.handler;

import cn.hutool.core.io.FileUtil;
import com.caixy.adminSystem.common.api.file.dto.FileInfoDTO;
import com.caixy.adminSystem.common.base.chain.ChainContext;
import com.caixy.adminSystem.common.base.chain.ChainHandler;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.exception.ErrorCode;
import com.caixy.adminSystem.infrastructure.file.domain.dto.FileSaveInfo;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadContext;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadFileDTO;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadFileRequest;
import com.caixy.adminSystem.common.api.file.enums.FileActionBizEnum;
import com.caixy.adminSystem.infrastructure.file.strategy.UploadFileMethodStrategy;
import lombok.RequiredArgsConstructor;

import java.nio.file.Paths;

/**
 * 秒传前置处理器
 *
 * @Author CAIXYPROMISE
 * @since 2025/5/29 3:52
 */
@RequiredArgsConstructor
public class FasterUploadPrepareHandler implements ChainHandler<UploadContext, String>
{
    @Override
    public void handle(ChainContext<UploadContext, String> context)
    {
        UploadContext contextData = context.getData();
        FileInfoDTO fileInfo = contextData.getFileInfo();
        if (fileInfo == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件不存在，无法秒传");
        }
        contextData.setFileInfo(fileInfo);
        UploadFileRequest req = contextData.getUploadFileRequest();
        Long userId = contextData.getUserId();
        FileActionBizEnum bizEnum = req.getBiz();

        // 推断文件后缀
        String fileRealName = req.getFileName();
        String fileSuffix = FileUtil.getSuffix(fileRealName);

        UploadFileMethodStrategy uploadFileMethodStrategy = contextData.getUploadFileMethodStrategy();

        // 构造FileSaveInfo
        FileSaveInfo fileSaveInfo = FileSaveInfo.builder()
                                                              .fileInnerName(fileInfo.getFileInnerName())
                                                              .fileRealName(fileRealName)
                                                              .fileSuffix(fileSuffix)
                                                              .filePath(Paths.get(fileInfo.getStoragePath()))
                                                              .fileURL(uploadFileMethodStrategy.buildFileURL(userId, fileInfo.getFileInnerName(), bizEnum))
                                                              .contentType(fileInfo.getContentType())
                                                              .build();

        // 构造UploadFileDTO
        UploadFileDTO uploadFileDTO = UploadFileDTO.builder()
                                                   .userId(userId)
                                                   .fileActionBizEnum(bizEnum)
                                                   .fileSaveInfo(fileSaveInfo)
                                                   .sha256(fileInfo.getFileSha256())
                                                   .fileSize(fileInfo.getFileSize())
                                                   .build();

        contextData.setUploadFileDTO(uploadFileDTO);
    }
}
