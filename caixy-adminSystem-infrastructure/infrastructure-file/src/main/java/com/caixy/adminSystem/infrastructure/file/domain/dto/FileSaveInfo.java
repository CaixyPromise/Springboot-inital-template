package com.caixy.adminSystem.infrastructure.file.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

/**
 * 文件保存时的状态信息
 *
 * @Author CAIXYPROMISE
 * @since 2025/6/23 11:11
 */
@Builder
@Getter
@Setter
public class FileSaveInfo
{
    /**
     * 文件唯一标识
     */
    private String uuid;

    /**
     * 文件内部名称
     */
    private String fileInnerName;

    /**
     * 文件真实名称
     */
    private String fileRealName;

    /**
     * 文件扩展名称
     */
    private String fileSuffix;

    /**
     * 文件保存文件夹路径
     */
    private Path filePath;

    /**
     * 文件可访问路径
     */
    private String fileURL;

    private String contentType;
}
