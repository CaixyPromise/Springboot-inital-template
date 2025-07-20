package com.caixy.adminSystem.common.api.file.dto;

import lombok.*;

/**
 * 文件引用DTO
 *
 * @Author CAIXYPROMISE
 * @since 2025/6/30 上午12:28
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileReferenceDTO
{
    /**
     * 关联文件ID
     */
    private Long fileId;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 展示名称
     */
    private String displayName;
    /**
     * 文件网络访问地址
     */
    private String visitUrl;
    /**
     * 文件业务类型
     */
    private String bizType;

    /**
     * 文件关联业务的字段 ID
     */
    private Long bizId;

    /**
     * 访问权限级别（0私有，1登录可见，2公开）
     */
    private Integer accessLevel;
}
