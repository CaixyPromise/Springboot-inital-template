package com.caixy.adminSystem.infrastructure.file.chain;

import com.caixy.adminSystem.common.api.file.facade.FileEntityFacade;
import com.caixy.adminSystem.common.base.chain.ChainExecutor;
import com.caixy.adminSystem.infrastructure.file.chain.handler.*;
import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadContext;
import com.caixy.adminSystem.infrastructure.file.strategy.UploadFileMethodStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 上传业务责任链工厂
 *
 * @Author CAIXYPROMISE
 * @since 2025/5/27 2:09
 */
@Component
@RequiredArgsConstructor
public class UploadChainFactory
{
    private final FileEntityFacade fileEntityFacade;
    /**
     * 秒传业务逻辑
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/5/29 3:25
     */
    public String doFasterUpload(UploadContext uploadContext)
    {
        ChainExecutor<UploadContext, String> executor = new ChainExecutor<>();
        executor.addHandler(new FasterUploadPrepareHandler())
                .addHandler(new FasterUploadValidHandler())
                .addHandler(new DoBeforeActionHandler())
                .addHandler(new DoAfterActionHandler())
                .addHandler(new SystemAfterActionHandler(fileEntityFacade));
        return executor.execute(uploadContext);
    }

    /**
     * 普通上传逻辑
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/5/29 3:25
     */
    public String doNormalUpload(UploadContext uploadContext)
    {
        UploadFileMethodStrategy uploadFileMethodStrategy = uploadContext.getUploadFileMethodStrategy();
        ChainExecutor<UploadContext, String> executor = new ChainExecutor<>();
        executor.addHandler(new NormalUploadPrepareHandler())
                .addHandler(new DoBeforeActionHandler())
                .addHandler(new DoSaveFileHandler(uploadFileMethodStrategy, fileEntityFacade))
                .addHandler(new DoAfterActionHandler())
                .addHandler(new SystemAfterActionHandler(fileEntityFacade));
        return executor.execute(uploadContext);
    }
}
