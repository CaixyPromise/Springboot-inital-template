package com.caixy.adminSystem.business.post.domain.postfavour.entity;

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
 * 帖子收藏
 *
 
 **/
@EqualsAndHashCode(callSuper = true)
@TableName(value = "post_favour")
@Data
public class PostFavour extends BaseEntity
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