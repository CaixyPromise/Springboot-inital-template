package com.caixy.adminSystem.infrastructure.file.chain.handler;

import com.caixy.adminSystem.common.api.file.dto.FileUploadAfterActionResult;
import com.caixy.adminSystem.common.base.chain.ChainContext;
import com.caixy.adminSystem.common.base.chain.ChainHandler;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.exception.ErrorCode;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadContext;
import com.caixy.adminSystem.infrastructure.file.strategy.FileActionStrategy;

/**
 * 业务后置校验
 *
 * @Author CAIXYPROMISE
 * @since 2025/5/27 1:29
 */
public class DoAfterActionHandler implements ChainHandler<UploadContext, String>
{
    @Override
    public void handle(ChainContext<UploadContext, String> context)
    {
        UploadContext contextData = context.getData();
        FileActionStrategy fileActionStrategy = contextData.getFileActionStrategy();
        FileUploadAfterActionResult afterActionResult = fileActionStrategy.doAfterUploadAction(
                contextData,
                contextData.getFileActionHelper(),
                contextData.getSavePath(),
                contextData.getUploadFileRequest(),
                contextData.getRequest());
        if (!afterActionResult.getSuccess()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        contextData.setAfterActionResult(afterActionResult);
    }
}
