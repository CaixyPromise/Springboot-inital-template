package com.caixy.adminSystem.business.post.domain.postthumb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.caixy.adminSystem.infrastructure.datasource.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子点赞
 *
 
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "post_thumb")
@Data
public class PostThumb extends BaseEntity
{
    /**
     * 帖子 id
     */
    private Long postId;

    /**
     * 创建用户 id
     */
    private Long userId;
}