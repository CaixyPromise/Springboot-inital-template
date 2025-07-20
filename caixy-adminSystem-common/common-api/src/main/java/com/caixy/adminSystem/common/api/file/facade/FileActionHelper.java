package com.caixy.adminSystem.common.api.file.facade;

import com.caixy.adminSystem.common.api.file.dto.FileReferenceDTO;

import java.util.List;

/**
 * 文件业务服务门面类
 *
 * @Author CAIXYPROMISE
 * @since 2025/6/30 上午12:26
 */
public interface FileActionHelper
{
    Boolean removeFile(Long fileId, Long userId, String bizType, Long bizId);

    Boolean batchRemoveByIds(List<Long> fileIds, Long userId, String bizType, Long bizId);

    List<FileReferenceDTO> listFileByBiz(Long userId, String bizType, Long bizId);
}
