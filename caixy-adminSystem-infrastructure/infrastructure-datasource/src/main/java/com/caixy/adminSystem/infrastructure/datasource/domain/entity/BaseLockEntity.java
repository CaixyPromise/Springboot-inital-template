package com.caixy.adminSystem.infrastructure.datasource.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 基础带有乐观锁模型对象
 *
 * @Author CAIXYPROMISE
 * @since 2025/1/1 18:52
 */
@Getter
@Setter
@ToString
public class BaseLockEntity extends BaseEntity
{
    /**
     * 乐观锁版本号
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer lockVersion;
}
