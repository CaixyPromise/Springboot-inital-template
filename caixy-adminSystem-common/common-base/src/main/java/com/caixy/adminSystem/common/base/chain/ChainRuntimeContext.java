package com.caixy.adminSystem.common.base.chain;

import java.util.*;
import java.util.function.Supplier;

/**
 * 责任执行链运行时上下文（懒加载 & 组合式 Map）
 *
 * @author CAIXYPROMISE
 * @since 2025/6/25
 */
public class ChainRuntimeContext {
    /**
     * 空 Map 哨兵，避免无谓分配
     */
    private static final Map<String, Object> EMPTY = Collections.emptyMap();

    /** 底层数据容器，懒初始化 */
    private Map<String, Object> delegate = EMPTY;

    /** 预期元素数量，用于初始化容量 */
    private final int expectedSize;

    /**
     * 默认构造，expectedSize = 0（第一次写入时按默认容量创建）
     */
    public ChainRuntimeContext() {
        this(0);
    }

    /**
     * 构造时指定预期容量，避免扩容开销
     * @param expectedSize 预期的键值对数量
     */
    public ChainRuntimeContext(int expectedSize) {
        this.expectedSize = Math.max(0, expectedSize);
    }

    /**
     * 确保底层 Map 已经可写、按需分配
     */
    private void ensureMutable() {
        if (delegate == EMPTY) {
            // 计算一个合理的初始容量：至少16，或根据 expectedSize / loadFactor
            int cap = (expectedSize > 0)
                    ? (int) (expectedSize / 0.75f) + 1
                    : 16;
            delegate = new HashMap<>(cap);
        }
    }

    /**
     * 类型安全地获取值
     * @param key   键
     * @param type  期望类型
     * @param <T>   类型参数
     * @return      如果存在且类型匹配，则返回非空 Optional，否则 Optional.empty()
     */
    public <T> Optional<T> get(String key, Class<T> type) {
        Object v = delegate.get(key);
        if (type.isInstance(v)) {
            return Optional.of(type.cast(v));
        }
        return Optional.empty();
    }

    public Optional<Object> get(String key) {
        return Optional.ofNullable(delegate.get(key));
    }

    /**
     * 带默认值的获取
     */
    public <T> T getOrDefault(String key, T defaultValue, Class<T> type) {
        return get(key, type).orElse(defaultValue);
    }

    /**
     * 流式 put，懒初始化后写入
     */
    public ChainRuntimeContext set(String key, Object value) {
        ensureMutable();
        delegate.put(key, value);
        return this;
    }

    /**
     * 是否包含某个 key
     */
    public boolean hasKey(String key) {
        return delegate.containsKey(key);
    }

    /**
     * 移除指定 key
     */
    public ChainRuntimeContext removeKey(String key) {
        if (delegate != EMPTY) {
            delegate.remove(key);
        }
        return this;
    }

    /**
     * 清空上下文，恢复到 EMPTY 状态
     */
    public void clear() {
        if (delegate != EMPTY) {
            delegate.clear();
            delegate = EMPTY;
        }
    }

    /**
     * 返回所有 key 的只读视图
     */
    public Set<String> keys() {
        return Collections.unmodifiableSet(delegate.keySet());
    }

    /**
     * 判断是否为空上下文（无任何写入）
     */
    public boolean isEmptyContext() {
        return delegate.isEmpty();
    }

    /**
     * 快照当前上下文，返回只读副本
     */
    public Map<String, Object> snapshot() {
        if (delegate == EMPTY) {
            return EMPTY;
        }
        return Collections.unmodifiableMap(new HashMap<>(delegate));
    }

    /**
     * 如果不存在则通过 supplier 生成并写入，返回最终值
     */
    @SuppressWarnings("unchecked")
    public <T> T computeIfAbsent(String key, Supplier<? extends T> supplier) {
        ensureMutable();
        return (T) delegate.computeIfAbsent(key, k -> supplier.get());
    }

    /**
     * 合并 value（若已有则用 remappingFunction 决定新值）
     */
    @SuppressWarnings("unchecked")
    public ChainRuntimeContext merge(
            String key,
            Object value,
            java.util.function.BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction
    ) {
        ensureMutable();
        delegate.merge(key, value, remappingFunction);
        return this;
    }
}
