package com.caixy.adminSystem.business.file.facade;

import com.caixy.adminSystem.business.file.infrastructure.convertor.FileEntityConvertor;
import com.caixy.adminSystem.business.file.services.FileInfoService;
import com.caixy.adminSystem.business.file.services.FileReferenceService;
import com.caixy.adminSystem.common.api.file.dto.FileInfoDTO;
import com.caixy.adminSystem.common.api.file.dto.FileReferenceDTO;
import com.caixy.adminSystem.common.api.file.dto.FileUploadAfterActionResult;
import com.caixy.adminSystem.common.api.file.facade.FileEntityFacade;
import com.caixy.adminSystem.common.api.file.enums.FileActionBizEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 文件数据库门面类实现
 *
 * @Author CAIXYPROMISE
 * @since 2025/6/30 上午12:35
 */
@Getter
@Component
@RequiredArgsConstructor
public class FileEntityFacadeImpl implements FileEntityFacade
{
    private final FileInfoService fileInfoService;
    private final FileReferenceService fileReferenceService;
    private static final FileEntityConvertor fileEntityConvertor = FileEntityConvertor.INSTANCE;

    /**
     * 根据sha256和文件大小查找文件信息
     * 
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/6/30 上午1:10
     */
    @Override
    public FileInfoDTO findFileBySha256AndSize(String sha256, Long fileSize)
    {
        // 返回查询结果
        return fileEntityConvertor.of(fileInfoService.findFileBySha256AndSize(sha256, fileSize));
    }

    @Override
    public FileReferenceDTO bindFileReference(Long fileId, Long userId, FileActionBizEnum bizEnum, FileUploadAfterActionResult after) {
        return fileEntityConvertor.of(fileReferenceService.bindFileReference(fileId, userId, bizEnum, after));
    }

    @Override
    public Boolean saveFileInfo(FileInfoDTO fileInfoDTO) {
        return fileInfoService.save(fileEntityConvertor.of(fileInfoDTO));
    }

}
