package com.caixy.adminSystem.common.api.file.dto;

import lombok.*;

import java.io.Serializable;

/**
 * 文件信息DTO
 *
 * @Author CAIXYPROMISE
 * @since 2025/6/29 下午11:13
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoDTO implements Serializable
{
    private Long id;

    /**
     * 文件内部名称加密存储
     */
    private String fileInnerName;

    /**
     * 文件大小（单位：字节）
     */
    private Long fileSize;

    /**
     * 文件sha256值
     */
    private String fileSha256;

    /**
     * 存储 MIME 类型（如 image/png、application/pdf）
     */
    private String contentType;

    /**
     * 存储类型（如本地存储、阿里云OSS、腾讯云COS）
     */
    private Integer storageType;

    /**
     * 文件存储路径
     */
    private String storagePath;
}
