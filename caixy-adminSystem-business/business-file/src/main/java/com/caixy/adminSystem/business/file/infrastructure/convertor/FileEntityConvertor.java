package com.caixy.adminSystem.business.file.infrastructure.convertor;

import com.caixy.adminSystem.business.file.domain.entity.FileInfo;
import com.caixy.adminSystem.business.file.domain.entity.FileReference;
import com.caixy.adminSystem.common.api.file.dto.FileInfoDTO;
import com.caixy.adminSystem.common.api.file.dto.FileReferenceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 文件实体转化器
 *
 * @Author CAIXYPROMISE
 * @since 2025/6/30 上午12:39
 */
@Mapper
public interface FileEntityConvertor
{
    FileEntityConvertor INSTANCE = Mappers.getMapper(FileEntityConvertor.class);

    // 文件信息DTO转实体
    FileInfoDTO of(FileInfo fileInfo);

    // 文件引用DTO转实体
    FileReferenceDTO of(FileReference fileReference);

    FileInfo of(FileInfoDTO fileInfoDTO);
}
