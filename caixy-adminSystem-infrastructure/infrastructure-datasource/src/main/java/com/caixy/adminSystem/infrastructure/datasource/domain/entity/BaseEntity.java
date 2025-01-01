package com.caixy.adminSystem.infrastructure.datasource.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础模型对象
 *
 * @Author CAIXYPROMISE
 * @since 2025/1/1 18:46
 */
@Getter
@Setter
@ToString
public class BaseEntity implements Serializable
{
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;

    /**
    * 创建时间
    */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
    * 更新时间
    */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
