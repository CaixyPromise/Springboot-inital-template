package com.caixy.adminSystem.model.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Es的分页类
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.common.EsPage
 * @since 2024-06-28 18:58
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class EsPage<T> extends Page<T>
{
    private List<Object> searchAfter;

    public EsPage(long current, long size)
    {
        super(current, size);
    }

    public EsPage(long current, long size, List<Object> searchAfter)
    {
        super(current, size);
        this.searchAfter = searchAfter;
    }
}
