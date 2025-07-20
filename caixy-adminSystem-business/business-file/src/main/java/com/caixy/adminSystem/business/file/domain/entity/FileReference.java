package com.caixy.adminSystem.business.file.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.caixy.adminSystem.infrastructure.datasource.domain.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 文件引用表
 * @TableName t_file_reference
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="t_file_reference")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class FileReference extends BaseEntity {

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