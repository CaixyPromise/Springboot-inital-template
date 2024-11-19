package com.caixy.adminSystem.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.caixy.adminSystem.utils.StringUtils;

/**
 * SQL 工具
 *
 
 */
public class SqlUtils {

    /**
     * 校验排序字段是否合法（防止 SQL 注入）
     *
     * @param sortField
     * @return
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }
}
