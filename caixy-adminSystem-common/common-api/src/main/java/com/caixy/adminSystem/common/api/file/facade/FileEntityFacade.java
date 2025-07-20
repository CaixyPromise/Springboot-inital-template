package com.caixy.adminSystem.common.api.file.facade;


import com.caixy.adminSystem.common.api.file.dto.FileInfoDTO;
import com.caixy.adminSystem.common.api.file.dto.FileReferenceDTO;
import com.caixy.adminSystem.common.api.file.dto.FileUploadAfterActionResult;
import com.caixy.adminSystem.common.api.file.enums.FileActionBizEnum;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文件数据库门面类
 *
 * @Author CAIXYPROMISE
 * @since 2025/6/30 上午12:34
 */
public interface FileEntityFacade
{
    // 根据sha256和文件大小查找文件信息
    FileInfoDTO findFileBySha256AndSize(String sha256, Long fileSize);

    FileReferenceDTO bindFileReference(Long fileId, Long userId, FileActionBizEnum bizEnum, FileUploadAfterActionResult after);

    Boolean saveFileInfo(FileInfoDTO fileInfoDTO);
}
