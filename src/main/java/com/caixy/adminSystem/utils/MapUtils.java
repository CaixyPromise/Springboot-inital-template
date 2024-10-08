package com.caixy.adminSystem.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Map工具类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.utils.MapUtils
 * @since 2024/10/8 下午11:41
 */
public final class MapUtils
{

    /**
     * 创建一个空的 HashMap
     *
     * @return 一个新的空 HashMap
     */
    public static <K, V> Map<K, V> newHashMap()
    {
        return new HashMap<>();
    }

    /**
     * 根据键列表和值列表创建一个 HashMap
     *
     * @param keyList   键的列表
     * @param valueList 值的列表
     * @return 一个新的包含给定键值对的 HashMap
     */
    public static <K, V> Map<K, V> newHashMap(List<K> keyList, List<V> valueList)
    {
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < keyList.size(); i++)
        {
            map.put(keyList.get(i), valueList.get(i));
        }
        return map;
    }

    /**
     * 从 Map 中获取指定类型的值
     *
     * @param map  目标 Map
     * @param key  要获取的键
     * @param type 返回值的类型
     * @param <V>  返回值的泛型类型
     * @return 指定类型的值，或 null 如果不存在
     */
    @SuppressWarnings("unchecked")
    public static <V> V safetyGetValueByKey(Map<String, Object> map, String key, Class<V> type)
    {
        if (map == null || map.isEmpty())
        {
            return null;
        }
        Object value = map.get(key);
        if (type.isInstance(value))
        {
            return (V) value;
        }
        return null;
    }

    /**
     * 合并多个 Map
     *
     * @param maps 多个 Map
     * @param <K>  键的类型
     * @param <V>  值的类型
     * @return 合并后的 Map
     */
    @SafeVarargs
    public static <K, V> Map<K, V> mergeMaps(Map<K, V>... maps)
    {
        Map<K, V> result = new HashMap<>();
        if (maps != null)
        {
            for (Map<K, V> map : maps)
            {
                if (map != null)
                {
                    result.putAll(map);
                }
            }
        }
        return result;
    }

    /**
     * 根据键的条件过滤 Map
     *
     * @param map       需要过滤的 Map
     * @param predicate 键的条件
     * @param <K>       键的类型
     * @param <V>       值的类型
     * @return 过滤后的 Map
     */
    public static <K, V> Map<K, V> filterByKeys(Map<K, V> map, Predicate<K> predicate)
    {
        Map<K, V> result = new HashMap<>();
        if (map != null && predicate != null)
        {
            for (Map.Entry<K, V> entry : map.entrySet())
            {
                if (predicate.test(entry.getKey()))
                {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }

    /**
     * 根据值的条件过滤 Map
     *
     * @param map       需要过滤的 Map
     * @param predicate 值的条件
     * @param <K>       键的类型
     * @param <V>       值的类型
     * @return 过滤后的 Map
     */
    public static <K, V> Map<K, V> filterByValues(Map<K, V> map, Predicate<V> predicate)
    {
        Map<K, V> result = new HashMap<>();
        if (map != null && predicate != null)
        {
            for (Map.Entry<K, V> entry : map.entrySet())
            {
                if (predicate.test(entry.getValue()))
                {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }

    /**
     * 反转 Map 的键值对
     *
     * @param map 需要反转的 Map
     * @param <K> 键的类型
     * @param <V> 值的类型
     * @return 反转后的 Map
     */
    public static <K, V> Map<V, K> invertMap(Map<K, V> map)
    {
        Map<V, K> invertedMap = new HashMap<>();
        if (map != null)
        {
            for (Map.Entry<K, V> entry : map.entrySet())
            {
                invertedMap.put(entry.getValue(), entry.getKey());
            }
        }
        return invertedMap;
    }

    /**
     * 将一个 Map 转换为 HashMap
     *
     * @param map 需要转换的 Map
     * @return 转换后的 HashMap
     */
    public static <K, V> HashMap<K, V> toHashMap(Map<K, V> map)
    {
        return map == null ? new HashMap<>() : new HashMap<>(map);
    }

    /**
     * 将一个 HashMap 转换为通用 Map 接口
     *
     * @param hashMap 需要转换的 HashMap
     * @return 转换后的 Map
     */
    public static <K, V> Map<K, V> fromHashMap(HashMap<K, V> hashMap)
    {
        return hashMap == null ? new HashMap<>() : hashMap;
    }

    /**
     * 检查 Map 是否包含指定的 key
     *
     * @param map 需要检查的 Map
     * @param key 需要查找的 key
     * @return 如果 Map 包含 key 则返回 true，否则返回 false
     */
    public static <K, V> boolean containsKey(Map<K, V> map, K key)
    {
        return map != null && map.containsKey(key);
    }

    /**
     * 检查 Map 是否包含指定的 value
     *
     * @param map   需要检查的 Map
     * @param value 需要查找的 value
     * @return 如果 Map 包含 value 则返回 true，否则返回 false
     */
    public static <K, V> boolean containsValue(Map<K, V> map, V value)
    {
        return map != null && map.containsValue(value);
    }
}
