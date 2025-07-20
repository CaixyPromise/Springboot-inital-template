package com.caixy.adminSystem.common.base.utils.hmac.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.codec.binary.Hex;

import java.security.SecureRandom;

/**
 * 基础HMAC实体类
 *
 * @Author CAIXYPROMISE
 * @since 2025/6/23 19:15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class HmacPayload
{
    /** 服务器端私有盐（绝不下发前端） */
    @JsonIgnore
    private String salt;

    /** 一次性随机串，防重放 */
    private String nonce;

    /** 挑战原文 */
    private String challenge;

    /** 秒或毫秒级时间戳，用于过期校验 */
    private Long timestamp;

    private static final SecureRandom RND = new SecureRandom();

    public static HmacPayload build() {
        byte[] saltBytes = randomBytes(32);
        byte[] nonceBytes = randomBytes(16);
        byte[] challBytes = randomBytes(16);

        return HmacPayload.builder()
                .salt(Hex.encodeHexString(saltBytes))
                .nonce(Hex.encodeHexString(nonceBytes))
                .challenge(Hex.encodeHexString(challBytes))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /** 产生指定长度的随机字节 */
    private static byte[] randomBytes(int len) {
        byte[] b = new byte[len];
        RND.nextBytes(b);
        return b;
    }
}
