package com.caixy.adminSystem.infrastructure.file.chain.handler;


import com.caixy.adminSystem.common.api.file.dto.FileUploadBeforeActionResult;
import com.caixy.adminSystem.common.base.chain.ChainContext;
import com.caixy.adminSystem.common.base.chain.ChainHandler;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.exception.ErrorCode;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadContext;
import com.caixy.adminSystem.infrastructure.file.strategy.FileActionStrategy;

/**
 * 业务前置校验（权限、配额等）
 *
 * @Author CAIXYPROMISE
 * @since 2025/5/27 1:29
 */
public class DoBeforeActionHandler implements ChainHandler<UploadContext, String>
{

    @Override
    public void handle(ChainContext<UploadContext, String> context)
    {
        UploadContext contextData = context.getData();
        FileActionStrategy fileActionStrategy = contextData.getFileActionStrategy();
        FileUploadBeforeActionResult fileUploadBeforeActionResult = fileActionStrategy.doBeforeUploadAction(contextData, contextData.getFileActionHelper(), contextData.getUploadFileRequest(), contextData.getRequest());
        if (!fileUploadBeforeActionResult.getSuccess()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        contextData.setBeforeActionResult(fileUploadBeforeActionResult);
    }
}
