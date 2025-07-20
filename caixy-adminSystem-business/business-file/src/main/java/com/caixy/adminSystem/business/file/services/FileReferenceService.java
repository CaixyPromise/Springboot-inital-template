package com.caixy.adminSystem.business.file.services;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caixy.adminSystem.business.file.domain.entity.FileReference;
import com.caixy.adminSystem.common.api.file.dto.FileReferenceDTO;
import com.caixy.adminSystem.common.api.file.dto.FileUploadAfterActionResult;
import com.caixy.adminSystem.common.api.file.enums.FileActionBizEnum;

import java.util.List;

/**
* @author CAIXYPROMISE
* @description 针对表【t_file_reference(文件引用表)】的数据库操作Service
* @createDate 2025-04-22 19:15:08
*/
public interface FileReferenceService extends IService<FileReference> {

    FileReference bindFileReference(Long fileId, Long userId, FileActionBizEnum fileActionBizEnum, FileUploadAfterActionResult afterActionResult);

    Boolean removeFileReferenceById(Long fileId, Long userId, String bizType, Long bizId);

    Boolean removeFileReferencesByFileIds(List<Long> fileIds, Long userId, String bizType, Long bizId);

    List<FileReferenceDTO> listFileReferenceByBiz(Long userId, String bizType, Long bizId);

    FileReference findFileReferenceByBiz(Long userId, String bizType, Long bizId);

    Boolean isSameFile(String bizType, Long bizId, Long userId, String sha256, Long fileSize);
}
