package com.caixy.adminSystem.infrastucture.cache.redis;

import com.caixy.adminSystem.common.base.constant.BaseCacheEnum;
import com.caixy.adminSystem.common.base.utils.JsonUtils;
import com.caixy.adminSystem.common.base.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存操作类
 *
 * @name: com.caixy.project.utils.RedisUtils
 * @author: CAIXYPROMISE
 * @since: 2023-12-20 20:14
 **/
@Component
@AllArgsConstructor
@Slf4j
public class RedisManager
{
    // 调用接口排名信息的最大容量
    private static final Long REDIS_RANK_MAX_SIZE = 10L;

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, Object> objectRedisTemplate;
    private static final String LUA_GET_DEL_SRC =
            "local v = redis.call('GET', KEYS[1]); " +
                    "if v ~= false then redis.call('DEL', KEYS[1]); end; " +
                    "return v;";

    private static final String LUA_HGETALL_DEL_SRC =
            "local e = redis.call('HGETALL', KEYS[1]); " +
                    "if next(e) ~= nil then redis.call('DEL', KEYS[1]); end; " +
                    "return e;";

    /** ------------ 预编译脚本对象 ------------ */
    private static final RedisScript<String> LUA_GET_DEL_SCRIPT;
    private static final RedisScript<List> LUA_HGETALL_DEL_SCRIPT;

    static {
        DefaultRedisScript<String> getDel = new DefaultRedisScript<>();
        getDel.setScriptText(LUA_GET_DEL_SRC);
        getDel.setResultType(String.class);
        LUA_GET_DEL_SCRIPT = getDel;

        DefaultRedisScript<List> hgetallDel = new DefaultRedisScript<>();
        hgetallDel.setScriptText(LUA_HGETALL_DEL_SRC);
        hgetallDel.setResultType(List.class);
        LUA_HGETALL_DEL_SCRIPT = hgetallDel;
    }

    /**
     * 删除Key的数据
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/12/10 20:19
     */
    public boolean delete(String key)
    {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }

    public boolean delete(List<String> keys)
    {
        Long delete = stringRedisTemplate.delete(keys);
        return delete != null && delete > 0;
    }

    /**
     * 删除Key的数据：接受来自常量的配置
     *
     * @author CAIXYPROMISE
     * @version 2.0
     * @since 2024/2/16 20:19
     */
    public boolean delete(BaseCacheEnum Enum, Object... items)
    {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(Enum.generateKey(items)));
    }

    /**
     * 刷新过期时间：接受来自常量的配置
     *
     * @author CAIXYPROMISE
     * @version 2.0
     * @since 2024/2/16 20:19
     */
    public void settingExpire(BaseCacheEnum Enum, long expire, Object... items)
    {
        stringRedisTemplate.expire(Enum.generateKey(items), expire, Enum.getTimeUnit());
    }

    /**
     * 刷新过期时间
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/1220 20:18
     */
    public void settingExpire(String key, long expire, TimeUnit timeUnit)
    {
        stringRedisTemplate.expire(key, expire, timeUnit);
    }

    /**
     * 获取字符串数据：接受来自常量的配置
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/1220 20:18
     */
    public String getString(BaseCacheEnum Enum, Object... items)
    {
        return stringRedisTemplate.opsForValue().get(Enum.generateKey(items));
    }
    public String getStringThenRemove(BaseCacheEnum keyEnum, Object... items) {
        return getStringThenRemove(keyEnum.generateKey(items));
    }

    /**
     * 获取字符串数据
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/1220 20:18
     */
    public String getString(String key)
    {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public String getStringThenRemove(String key) {
        return stringRedisTemplate.execute(
                LUA_GET_DEL_SCRIPT,
                Collections.singletonList(key)
        );
    }


    public <JsonType> List<JsonType> getJsonList(BaseCacheEnum keyEnum, Object... items)
    {
        String cacheData = stringRedisTemplate.opsForValue().get(keyEnum.generateKey(items));
        if (StringUtils.isNotBlank(cacheData))
        {
            // 把字符串转义全部清楚掉
            return JsonUtils.jsonToList(cacheData);
        }
        // 直接返回空指针，不返回空列表
        return null;
    }
    public <T> List<T> getJsonListThenRemove(BaseCacheEnum keyEnum, Object... items) {
        String json = getStringThenRemove(keyEnum.generateKey(items));
        return StringUtils.isNotBlank(json) ? JsonUtils.jsonToList(json) : null;
    }

    public <JsonType> JsonType getJson(BaseCacheEnum keyEnum, Class<JsonType> returnType, Object... items)
    {
        String cacheData = stringRedisTemplate.opsForValue().get(keyEnum.generateKey(items));
        if (StringUtils.isNotBlank(cacheData))
        {
            return JsonUtils.jsonToObject(cacheData, returnType);
        }
        return null;
    }

    public <T> T getJsonThenRemove(BaseCacheEnum keyEnum, Class<T> type, Object... items) {
        String json = getStringThenRemove(keyEnum.generateKey(items));
        return StringUtils.isNotBlank(json) ? JsonUtils.jsonToObject(json, type) : null;
    }

    /**
     * 放入一个对象数据
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/6/16 上午10:32
     */
    public void setObject(BaseCacheEnum keyEnum, Object value, Object... items)
    {
        String key = keyEnum.generateKey(items);
        setObject(key, value, keyEnum.getExpire(), keyEnum.getTimeUnit());
    }

    public void setObject(BaseCacheEnum keyEnum, Object value, Long expire, Object... items)
    {
        String key = keyEnum.generateKey(items);
        if (expire != null && expire > 0)
        {
            setObject(key, value, expire, keyEnum.getTimeUnit());
            return;
        }
        // 如果过期时间为null和-1，则直接走默认时间配置（redis要求，过期时间必须大于0，如果需要永久只需要设置为null就行）
        setObject(keyEnum, value, items);
    }

    public void setObject(String key, Object value, Long expire, TimeUnit timeUnit)
    {
        setString(key, JsonUtils.toJsonString(value), expire, timeUnit);
    }

    /**
     * 从redis获取对象，因为放入时已经处理序列化，所以在这里需要从json转对象
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/7/2 下午9:18
     */
    public <T> Optional<T> getObject(BaseCacheEnum keyEnum, Class<T> type, Object... items)
    {
        return getObject(keyEnum.generateKey(items), type);
    }

    public <T> Optional<T> getObjectThenRemove(BaseCacheEnum keyEnum,
                                               Class<T> type, Object... items) {
        return getObjectThenRemove(keyEnum.generateKey(items), type);
    }

    public <T> Optional<T> getObject(String key, Class<T> type)
    {
        String result = getString(key);
        if (StringUtils.isNotBlank(result))
        {
            return Optional.of(JsonUtils.jsonToObject(result, type));
        }
        return Optional.empty();
    }

    public <T> Optional<T> getObjectThenRemove(String key, Class<T> type) {
        String json = getStringThenRemove(key);
        return StringUtils.isNotBlank(json)
                ? Optional.of(JsonUtils.jsonToObject(json, type))
                : Optional.empty();
    }
    /**
     * 获取哈希数据：接受来自常量的配置
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/1220 20:18
     */
    public HashMap<String, Object> getHashMap(BaseCacheEnum Enum, Object... items)
    {
        Map<Object, Object> rawMap = objectRedisTemplate.opsForHash().entries(Enum.generateKey(items));
        HashMap<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<Object, Object> entry : rawMap.entrySet()) {
            resultMap.put(entry.getKey().toString(), entry.getValue());
        }
        return resultMap;
    }

    public HashMap<String,Object> getHashMapThenRemove(BaseCacheEnum keyEnum, Object... items) {
        String key = keyEnum.generateKey(items);

        List<Object> raw = stringRedisTemplate.execute(
                LUA_HGETALL_DEL_SCRIPT,
                Collections.singletonList(key)
        );

        HashMap<String, Object> map = new HashMap<>();
        if (raw != null) {
            for (int i = 0; i + 1 < raw.size(); i += 2) {
                map.put(raw.get(i).toString(), raw.get(i + 1));
            }
        }
        return map;
    }

    /**
     * 放入hash类型的数据 - Hash<String, Object>
     *
     * @param key    redis-key
     * @param data   数据
     * @param expire 过期时间, 单位: 秒
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/12/20 2:16
     */
    public <Key, Value> void setHashMap(String key, Map<Key, Value> data, Long expire, TimeUnit timeUnit) {
        // 直接存储原始对象，不进行任何序列化
        objectRedisTemplate.opsForHash().putAll(key, data);

        // 设置过期时间
        if (expire != null && expire > 0) {
            settingExpire(key, expire, timeUnit);
        }
    }

    /**
     * 放入hash类型的数据 - Hash<String, Object> 接受来自常量的配置
     *
     * @param data 数据
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/12/20 2:16
     */
    public <Key, Value> void setHashMap(BaseCacheEnum Enum, Map<Key, Value> data, Object... item)
    {
        Long expire = Enum.getExpire();
        String fullKey = Enum.generateKey(item);
        setHashMap(fullKey, data, expire, Enum.getTimeUnit());
    }

    /**
     * 放入字符串类型的字符
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/12/0 20:16
     */
    public void setString(BaseCacheEnum Enum, String value, Object... items)
    {
        setString(Enum.generateKey(items), value, Enum.getExpire(), Enum.getTimeUnit());
    }

    public void setString(String key, String value, Long expire, TimeUnit timeUnit)
    {
        if (expire > 0)
        {
            // 设置带过期时间的键值
            stringRedisTemplate.opsForValue().set(key, value, expire, timeUnit);
        }
        else
        {
            // 设置永久有效的键值
            stringRedisTemplate.opsForValue().set(key, value);
        }
    }


    /**
     * 获取是否有对应的Key值存在
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/12/20 12:25
     */
    public boolean hasKey(BaseCacheEnum redisEnum, Object... keyItems)
    {
        String key = redisEnum.generateKey(keyItems);
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    /**
     * 获取是否有对应的Key值存在
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/12/20 12:25
     */
    public boolean hasKey(String key)
    {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public Long getExpire(String key)
    {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 获取过期时间
     */
    public Long getExpire(String key, TimeUnit timeUnit)
    {
        return stringRedisTemplate.getExpire(key, timeUnit);
    }

    public Long getExpire(BaseCacheEnum keyEnum, Object... keyItem)
    {
        String key = keyEnum.generateKey(keyItem);
        return getExpire(key);
    }

    /**
     * 尝试在 Redis 中设置键值对（仅当键不存在时），并设置过期时间。
     *
     * @param key   键
     * @param value 值
     * @return 如果成功设置（键之前不存在），返回 true；否则返回 false
     */
    public Boolean setIfAbsent(BaseCacheEnum key, String value, Object... keyItem)
    {
        String keyStr = "IF_PRESENT:" + key.generateKey(keyItem);
        return setIfAbsent(keyStr, value, key.getExpire(), key.getTimeUnit());
    }

    public Boolean setIfAbsent(String key, String value, Long expire, TimeUnit timeUnit)
    {
        if (expire > 0)
        {
            // 尝试设置键值并指定过期时间
            return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, value, expire, timeUnit));
        }
        // 尝试设置键值但不指定过期时间
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, value));
    }

    // region 集合操作

    /**
     * 向 Redis Set 中添加元素
     *
     * @param key    Redis 键
     * @param values 要添加的值
     */
    public Long addToSet(BaseCacheEnum key, String values, Object... keyItem)
    {
        return stringRedisTemplate.opsForSet().add(key.generateKey(keyItem), values);
    }

    /**
     * 向 Redis Set 中添加元素
     *
     * @param key    Redis 键
     * @param values 要添加的值
     */
    public void addToSet(String key, String... values)
    {
        stringRedisTemplate.opsForSet().add(key, values);
    }

    /**
     * 从 Redis Set 中移除元素
     *
     * @param key    Redis 键
     * @param values 要移除的值
     */
    public void removeFromSet(String key, String... values)
    {
        stringRedisTemplate.opsForSet().remove(key, (Object[]) values);
    }

    /**
     * 从 Redis Set 中移除元素
     *
     * @param key    Redis 键
     * @param values 要移除的值
     */
    public void removeFromSet(BaseCacheEnum key, Object[] values, Object... keyItems)
    {
        stringRedisTemplate.opsForSet().remove(key.generateKey(keyItems), values);
    }


    /**
     * 获取 Redis Set 中的所有元素
     *
     * @param key Redis 键
     * @return Set 集合
     */
    public Set<String> getMembersFromSet(String key)
    {
        return stringRedisTemplate.opsForSet().members(key);
    }

    /**
     * 获取 Redis Set 中的所有元素
     *
     * @param key Redis 键
     * @return Set 集合
     */
    public Set<String> getMembersFromSet(BaseCacheEnum key, Object... keyItem)
    {
        return stringRedisTemplate.opsForSet().members(key.generateKey(keyItem));
    }

    /**
     * 获取 Redis Set 的大小
     *
     * @param key Redis 键
     * @return 集合大小
     */
    public Long getSetSize(String key)
    {
        return stringRedisTemplate.opsForSet().size(key);
    }

    public Long getSetSize(BaseCacheEnum keyEnum, Object... keyItem)
    {
        String key = keyEnum.generateKey(keyItem);
        return getSetSize(key);
    }

    // endregion

    // region 排行榜实现
    // ===================== 排行榜实现 =====================

    /**
     * 有序集合添加之前没有的元素
     *
     * @param value 元素值 排行榜value-Key
     * @param score 分数
     * @return 是否添加成功
     * @author CAIXYPROMISE
     * @since 2023-12-29
     */
    public boolean zAdd(BaseCacheEnum rankKey, Object value, double score)
    {
        // 检查排行榜大小，并可能移除最低分数的记录
        manageRankSize(rankKey.getKey());
        // 添加新记录
        return Boolean.TRUE.equals(stringRedisTemplate.opsForZSet().add(rankKey.getKey(), value.toString(), score));
    }

    /**
     * 将HashMap转换为JSON字符串并添加到有序集合
     *
     * @param key   排行榜名称Key
     * @param map   要存储的HashMap
     * @param score 分数
     * @return 是否添加成功
     */
    public boolean zAddMap(BaseCacheEnum key, HashMap<String, Object> map, double score)
    {
        String valueAsJson = JsonUtils.mapToString(map);
        return zAdd(key, valueAsJson, score);
    }

    /**
     * 获取集合中元素的排名（从大到小排序）
     *
     * @param key   排行榜名称Key
     * @param value 元素值 排行榜value-Key
     * @return 获取到的排名
     * @author CAIXYPROMISE
     * @since 2023-12-29
     */
    public long zGetRank(String key, Object value)
    {
        return stringRedisTemplate.opsForZSet().reverseRank(key, value);
    }

    /**
     * 若集合中已有此元素，则此元素score+传入参数
     * 若没有此元素，则创建元素。
     *
     * @param key   排行榜名称Key
     * @param value 元素值 排行榜value-Key
     * @param score 分数
     * @author CAIXYPROMISE
     * @since 2023-12-29
     */
    public void zIncreamentScore(String key, Object value, double score)
    {
        stringRedisTemplate.opsForZSet().incrementScore(key, value.toString(), score);
    }

    /**
     * 检查排行榜大小，并可能移除最低分数的记录
     *
     * @param key 排行榜名称Key
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/1229 23:15
     */
    private void manageRankSize(String key)
    {
        Long size = zGetSize(key);
        if (size != null && size >= REDIS_RANK_MAX_SIZE)
        {
            // 移除最低分数的记录
            Set<ZSetOperations.TypedTuple<String>> lowestScoreSet = stringRedisTemplate.opsForZSet().rangeWithScores(key, 0, 0);
            if (lowestScoreSet != null && !lowestScoreSet.isEmpty())
            {
                Double lowestScore = lowestScoreSet.iterator().next().getScore();
                if (lowestScore != null)
                {
                    stringRedisTemplate.opsForZSet().removeRangeByScore(key, lowestScore, lowestScore);
                }
            }
        }
    }

    /**
     * 对集合按照分数从小到大排序（默认）
     * 指定位置区间0，-1指排序所有元素
     * 得到的值带有score
     *
     * @return 排序结果
     * @author CAIXYPROMISE
     * @since 2023-12-29
     */
    public Set<ZSetOperations.TypedTuple<String>> zRangeWithScore(String key)
    {
        return stringRedisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
    }

    /**
     * 对集合按照分数从大到小排序
     *
     * @param key 排行榜名称Key
     * @author CAIXYPROMISE
     * @since 2023-12-29
     */
    public Set<ZSetOperations.TypedTuple<String>> zReverseRangeWithScore(String key)
    {
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
    }

    /**
     * 获取有序集合的大小
     *
     * @param key 排行榜名称Key
     * @return 集合大小
     * @author CAIXYPROMISE
     * @since 2023-12-29
     */
    public Long zGetSize(String key)
    {
        return stringRedisTemplate.opsForZSet().size(key);
    }

    /**
     * 获取key集合里面，value值的分数
     *
     * @param key   排行榜名称Key
     * @param value 元素值 排行榜value-Key
     * @return 获取到的分数
     * @author CAIXYPROMISE
     * @since 2023-12-29
     */
    public Double zGetScoreByValue(String key, Object value)
    {
        return stringRedisTemplate.opsForZSet().score(key, value.toString());
    }


    /**
     * 指定分数区间，从大到小排序
     *
     * @param key   排行榜名称Key
     * @param start 开始范围
     * @param end   结束方位
     * @return 排序结果榜集合
     * @author CAIXYPROMISE
     * @since 2023-12-29
     */
    public Set<ZSetOperations.TypedTuple<String>> zReverseRangeByScoreWithScores(String key, double start, double end)
    {
        return stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, start, end);
    }

    public void zRemove(String key, Object value)
    {
        stringRedisTemplate.opsForZSet().remove(key, value.toString());
    }


    // endregion

    /**
     * 防重放验证：检查nonce是否已被使用，如果未使用则标记为已使用
     * 使用原子操作确保并发安全
     *
     * @param nonce 防重放标识
     * @param expire 过期时间
     * @param timeUnit 时间单位
     * @return true表示nonce有效且未被使用，false表示已被使用
     */
    public boolean checkAndSetNonce(String nonce, long expire, TimeUnit timeUnit) {
        String key = "nonce:" + nonce;
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, "1", expire, timeUnit));
    }

    /**
     * 防重放验证：使用默认过期时间（5分钟）
     *
     * @param nonce 防重放标识
     * @return true表示nonce有效且未被使用，false表示已被使用
     */
    public boolean checkAndSetNonce(String nonce) {
        return checkAndSetNonce(nonce, 5, TimeUnit.MINUTES);
    }
}
