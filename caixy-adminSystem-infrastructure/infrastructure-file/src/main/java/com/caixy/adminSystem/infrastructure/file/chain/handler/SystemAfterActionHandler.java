package com.caixy.adminSystem.infrastructure.file.chain.handler;

import com.caixy.adminSystem.common.api.file.dto.FileInfoDTO;
import com.caixy.adminSystem.common.api.file.dto.FileReferenceDTO;
import com.caixy.adminSystem.common.api.file.facade.FileEntityFacade;
import com.caixy.adminSystem.common.base.chain.ChainContext;
import com.caixy.adminSystem.common.base.chain.ChainHandler;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.exception.ErrorCode;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadContext;
import lombok.RequiredArgsConstructor;

/**
 * 系统最后处理处理器：绑定文件引用等操作。
 *
 * @Author CAIXYPROMISE
 * @since 2025/5/27 1:22
 */
@RequiredArgsConstructor
public class SystemAfterActionHandler implements ChainHandler<UploadContext, String>
{
    private final FileEntityFacade fileEntityFacade;

    @Override
    public void handle(ChainContext<UploadContext, String> context)
    {
        UploadContext contextData = context.getData();
        FileInfoDTO fileInfo = contextData.getFileInfo();
        FileReferenceDTO fileReference = fileEntityFacade.bindFileReference(fileInfo.getId(),
                                                                            contextData.getUserId(),
                                                                            contextData.getUploadFileDTO().getFileActionBizEnum(),
                                                                            contextData.getAfterActionResult());
        if (fileReference == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件引用绑定失败");
        }
        context.setResult(fileReference.getVisitUrl());
    }
}
