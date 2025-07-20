package com.caixy.adminSystem.business.file.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件信息表
 * @TableName t_file_info
 */
@TableName(value ="t_file_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileInfo implements Serializable {
    /**
     * 文件ID
     */
    @TableId
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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDeleted;

    /**
     * 删除时间
     */
    private Date deletedTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}