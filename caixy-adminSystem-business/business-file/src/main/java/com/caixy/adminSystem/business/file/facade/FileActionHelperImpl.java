package com.caixy.adminSystem.business.file.facade;

import com.caixy.adminSystem.business.file.domain.entity.FileReference;
import com.caixy.adminSystem.business.file.services.FileInfoService;
import com.caixy.adminSystem.business.file.services.FileReferenceService;
import com.caixy.adminSystem.common.api.file.dto.FileReferenceDTO;
import com.caixy.adminSystem.common.api.file.facade.FileActionHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件服务门面类
 *
 * @Author CAIXYPROMISE
 * @since 2025/6/30 上午12:26
 */
@Getter
@Component
@RequiredArgsConstructor
public class FileActionHelperImpl implements FileActionHelper
{
    private final FileInfoService fileInfoService;
    private final FileReferenceService fileReferenceService;

    /**
     * 删除文件 == 删除文件引用
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/4/23 3:10
     */
    @Override
    public Boolean removeFile(Long fileId, Long userId, String bizType, Long bizId) {
        return fileReferenceService.removeFileReferenceById(fileId, userId, bizType, bizId);
    }
    /**
     * 批量删除文件引用
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/5/26 16:51
     */
    @Override
    public Boolean batchRemoveByIds(List<Long> fileIds, Long userId, String bizType, Long bizId) {
        return fileReferenceService.removeFileReferencesByFileIds(fileIds, userId, bizType, bizId);
    }

    /**
     * 根据用户Id和业务信息查询文件引用
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/4/23 3:10
     */
    @Override
    public List<FileReferenceDTO> listFileByBiz(Long userId, String bizType, Long bizId) {
        return fileReferenceService.listFileReferenceByBiz(userId, bizType, bizId);
    }

    public FileReference findFileReferenceByBiz(Long userId, String bizType, Long bizId) {
        return fileReferenceService.findFileReferenceByBiz(userId, bizType, bizId);
    }

    public Boolean isSameFile(String bizType, Long bizId, Long userId, String sha256, Long fileSize) {
        return fileReferenceService.isSameFile(bizType, bizId, userId, sha256, fileSize);
    }
}
