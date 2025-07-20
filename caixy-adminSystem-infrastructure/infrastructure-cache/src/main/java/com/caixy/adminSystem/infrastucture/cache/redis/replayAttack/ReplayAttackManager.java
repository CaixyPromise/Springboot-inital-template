package com.caixy.adminSystem.infrastucture.cache.redis.replayAttack;

import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.exception.ErrorCode;
import com.caixy.adminSystem.common.base.utils.StringUtils;
import com.caixy.adminSystem.infrastucture.cache.redis.RedisManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 防重放攻击管理器
 *
 * @author CAIXYPROMISE
 * @version 1.0
 * @since 2025/1/27
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReplayAttackManager
{

    private final RedisManager redisManager;

    /**
     * 时间窗口容差（秒）
     */
    private static final long TIME_WINDOW_TOLERANCE = 300; // 5分钟

    /**
     * 验证防重放攻击
     *
     * @param nonce 防重放标识
     * @param timestamp 时间戳
     * @param userId 用户ID（可选，用于更精确的防重放）
     * @return true表示验证通过
     */
    public boolean validateReplayAttack(String nonce, Long timestamp, Long userId) {
        // 1. 验证参数
        if (StringUtils.isEmpty(nonce) || timestamp == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "防重放参数不完整");
        }

        // 2. 验证时间戳
        validateTimestamp(timestamp);

        // 3. 验证nonce唯一性
        String nonceKey = userId != null ? nonce + ":" + userId : nonce;
        if (!redisManager.checkAndSetNonce(nonceKey)) {
            log.warn("检测到重放攻击，nonce: {}, userId: {}, timestamp: {}", nonce, userId, timestamp);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求重复提交");
        }

        return true;
    }

    /**
     * 验证防重放攻击（简化版本）
     *
     * @param nonce 防重放标识
     * @param timestamp 时间戳
     * @return true表示验证通过
     */
    public boolean validateReplayAttack(String nonce, Long timestamp) {
        return validateReplayAttack(nonce, timestamp, null);
    }

    /**
     * 验证时间戳
     *
     * @param timestamp 时间戳
     */
    private void validateTimestamp(Long timestamp) {
        long currentTime = Instant.now().getEpochSecond();
        long requestTime = timestamp;

        // 检查时间戳是否在合理范围内
        if (Math.abs(currentTime - requestTime) > TIME_WINDOW_TOLERANCE) {
            log.warn("时间戳超出容差范围，当前时间: {}, 请求时间: {}, 容差: {}秒", 
                    currentTime, requestTime, TIME_WINDOW_TOLERANCE);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求时间戳无效");
        }
    }

    /**
     * 清理指定用户的nonce缓存（可选，用于手动清理）
     *
     * @param nonce 防重放标识
     * @param userId 用户ID
     */
    public void clearNonce(String nonce, Long userId) {
        String nonceKey = "nonce:" + nonce + ":" + userId;
        redisManager.delete(nonceKey);
    }
} 