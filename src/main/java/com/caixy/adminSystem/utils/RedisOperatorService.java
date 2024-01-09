package com.caixy.adminSystem.utils;

import com.caixy.adminSystem.constant.RedisConstant;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存操作类
 *
 * @name: com.caixy.project.utils.RedisOperatorService
 * @author: CAIXYPROMISE
 * @since: 2023-12-20 20:14
 **/
@Service
@AllArgsConstructor
public class RedisOperatorService
{
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 删除Key的数据
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/12/0 20:19
     */
    public boolean delete(String key)
    {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }

    /**
     * 刷新过期时间
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/1220 20:18
     */
    public void refreshExpire(String key, long expire)
    {
        stringRedisTemplate.expire(key, expire, TimeUnit.SECONDS);
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

    /**
     * 获取哈希数据
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/1220 20:18
     */
    public Map<Object, Object> getHash(String key, String objectName)
    {
        return stringRedisTemplate.opsForHash().entries(key + objectName);
    }

    /**
     * 放入hash类型的数据 - Hash<String, Object>
     * @param key redis-key
     * @param data 数据
     * @param expire 过期时间, 单位: 秒
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/12/20 2:16
     */
    public void setHashMap(String key, HashMap<String, Object> data, Long expire)
    {
        HashMap<String, String> stringData = new HashMap<>();
        data.forEach((dataKey, value) ->
                stringData.put(dataKey, JsonUtils.objectToString(value)));
        stringRedisTemplate.opsForHash().putAll(key, stringData);
        if (expire != null)
        {
            refreshExpire(key, expire);
        }
    }

    public void setStringHashMap(String key, HashMap<String, String> data, Long expire)
    {
        stringRedisTemplate.opsForHash().putAll(key, data);
        if (expire != null)
        {
            refreshExpire(key, expire);
        }
    }

    /**
     * 放入字符串类型的字符
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2023/12/0 20:16
     */
    public void setString(String key, String value, Long expire)
    {
        if (expire != null)
        {
            stringRedisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
        }
        else
        {
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
    public boolean hasKey(String key)
    {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }




    // ===================================== 分布式锁 =====================================
    /**
     * 尝试获取分布式锁
     *
     * @param lockKey    锁的Key
     * @param requestId  请求标识
     * @param expireTime 过期时间
     * @return 是否获取成功
     */
    public boolean tryGetDistributedLock(String lockKey, String requestId, Long expireTime)
    {
        for (int i = 0; i < RedisConstant.MAX_RETRY_TIMES; i++)
        {
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey,
                    requestId, expireTime, TimeUnit.SECONDS);
            if (result != null && result)
            {
                return true;
            }
            try
            {
                Thread.sleep(RedisConstant.RETRY_INTERVAL);
            } catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey   锁的Key
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public boolean releaseDistributedLock(String lockKey, String requestId)
    {
        if (requestId.equals(stringRedisTemplate.opsForValue().get(lockKey)))
        {
            return delete(lockKey);
        }
        return false;
    }

    // ===================== 排行榜实现 =====================

    /**
     * 有序集合添加之前没有的元素
     *
     * @param key   排行榜名称Key
     * @param value 元素值 排行榜value-Key
     * @param score 分数
     * @return 是否添加成功
     * @author CAIXYPROMISE
     * @since 2023-12-29
     */
    public boolean zAdd(String key, Object value, double score)
    {
        String lockKey = RedisConstant.REDIS_INVOKE_RANK_LOCK_KEY + key + ":lock";
        String requestId = UUID.randomUUID().toString();
        try
        {
            // 尝试获取分布式锁
            boolean isLocked = tryGetDistributedLock(lockKey, requestId, 10L);
            if (!isLocked)
            {
                return false; // 无法获取锁，直接返回
            }
            // 检查排行榜大小，并可能移除最低分数的记录
            manageRankSize(key);
            // 添加新记录
            return Boolean.TRUE.equals(stringRedisTemplate.opsForZSet().add(key, value.toString(), score));
        } finally
        {
            // 释放分布式锁
            releaseDistributedLock(lockKey, requestId);
        }
    }

    /**
     * 将HashMap转换为JSON字符串并添加到有序集合
     *
     * @param key   排行榜名称Key
     * @param map   要存储的HashMap
     * @param score 分数
     * @return 是否添加成功
     */
    public boolean zAddMap(String key, HashMap<String, Object> map, double score)
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
        if (size != null && size >= RedisConstant.REDIS_INVOKE_RANK_MAX_SIZE)
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
    public double zGetScoreByValue(String key, Object value)
    {
        return stringRedisTemplate.opsForZSet().score(key, value);
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

}
