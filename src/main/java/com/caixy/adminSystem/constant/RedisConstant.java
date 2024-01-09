package com.caixy.adminSystem.constant;

/**
 * @Name: com.caixy.project.constant.RedisConstant
 * @Description: Redis缓存的常量：Key和过期时间
 * @Author: CAIXYPROMISE
 * @Date: 2023-12-20 20:20
 **/
public interface RedisConstant
{
    /**
     * 随机数的redis key
     */
    String NONCE_KEY = "nonce:";
    /**
     * 随机数的过期时间，5分钟
     * */
    Long NONCE_EXPIRE = (60L * 5L);

    /**
     * 验证码缓存key
     * */
    String CAPTCHA_CODE_KEY = "captcha:";
    /**
     * 验证码过期时间：5分钟
     * */
    Long CAPTCHA_CODE_EXPIRE = (60L * 5L);

    String SIGNATURE_CODE_KEY = "signature:";
    Long SIGNATURE_CODE_EXPIRE = 60L;


    // 黑名单的Key
    String BLACKED_LIST_KEY = "BLACKED_LIST_KEY:";
    // 黑名单过期时间
    Long BACKED_LIST_EXPIRE = (60L * 60L * 24L);

    // 错误请求IP的统计redis计数
    String ERROR_COUNT_PREFIX   = "ERROR:COUNT:IP:";
    // 黑名单IP的redis-key
    String BLACKED_IP_PREFIX   = "BLACKED:IP:";
    // 错误IP统计次数过期时间
    Long ERROR_COUNT_DURATION  = (10L * 60L);
    // 黑名单IP的锁定时间
    Long BLACKLIST_DURATION = (60L * 60L);
    // 最大错误次数上限
    int MAX_ERROR_COUNT = 50;

    // 分布式锁基础定义Key
    String REDIS_LOCK_KEY = "REDIS_LOCK:";
    // 分布式锁定义过期时间
    Long REDIS_LOCK_EXPIRE = 5L;
    // 分布式锁最大重试次数
    int MAX_RETRY_TIMES = 10;
    // 分布式锁重试间隔时间（毫秒）
    Long RETRY_INTERVAL = 100L;

    // 分布式调用统计锁Key
    String REDIS_INVOKE_LOCK_KEY = REDIS_LOCK_KEY + "REDIS_INVOKE_LOCK";

    //  分布式调用统计排行榜插入锁Key过期时间
    String REDIS_INVOKE_RANK_LOCK_KEY = REDIS_LOCK_KEY + "REDIS_INVOKE_RANK_LOCK:";
    // 分布式调用统计锁过期时间
    Long REDIS_INVOKE_LOCK_EXPIRE_TIME = REDIS_LOCK_EXPIRE;


    // ================= 排行榜信息 ======================
    // 调用接口排名信息的最大容量
    Long REDIS_INVOKE_RANK_MAX_SIZE = 10L;

    String REDIS_INVOKE_INFO_KEY = "INFO:INVOKE_INFO";

    // 排名信息的Key
    String REDIS_RANK_KEY = "REDIS_KEY_RANK:";
    // 调用接口排名信息的Key
    String REDIS_INVOKE_RANK_KEY = REDIS_RANK_KEY + "INVOKE";
}
