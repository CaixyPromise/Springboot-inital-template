package com.caixy.adminSystem.business.file.domain.dto;

import com.caixy.adminSystem.business.file.domain.entity.FileInfo;
import com.caixy.adminSystem.business.file.domain.entity.FileReference;
import lombok.Data;

/**
 * 文件引用与文件信息表DTO，用于查询彼此关联信息进行返回封装
 *
 * @Author CAIXYPROMISE
 * @since 2025/4/23 3:34
 */
@Data
public class ReferenceWithFileInfoDTO
{
    /**
    * 文件信息
    */
    private FileInfo fileInfo;

    /**
    * 引用信息
    */
    private FileReference fileReference;
}
