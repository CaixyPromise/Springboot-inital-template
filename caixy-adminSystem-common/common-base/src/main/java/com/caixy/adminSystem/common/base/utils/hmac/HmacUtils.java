package com.caixy.adminSystem.common.base.utils.hmac;

import com.caixy.adminSystem.common.base.utils.hmac.domain.HmacPayload;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * Hmac工具类
 *
 * @Author CAIXYPROMISE
 * @since 2025/6/23 19:20
 */
public class HmacUtils
{
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * 生成 HMAC 签名（Hex）
     *
     * @param hmacPayload   必须包含：salt, nonce, challenge, timestamp
     * @param extras 可变参数数组，按顺序追加到待签名串末尾
     * @return       十六进制格式的 HMAC-SHA256 签名
     */
    public static String sign(HmacPayload hmacPayload, Object... extras) {
        Objects.requireNonNull(hmacPayload, "hmacPayload 不能为空");
        String salt = Objects.requireNonNull(hmacPayload.getSalt(), "salt 不能为空");

        // 1. 拼接基础字段：nonce + challenge + timestamp
        StringBuilder sb = new StringBuilder()
                .append(nullToEmpty(hmacPayload.getNonce()))
                .append(nullToEmpty(hmacPayload.getChallenge()))
                .append(hmacPayload.getTimestamp() == null ? "" : hmacPayload.getTimestamp().toString());

        // 2. 拼接可变业务值
        if (extras != null) {
            for (Object o : extras) {
                sb.append(o == null ? "" : o.toString());
            }
        }

        // 3. HMAC-SHA256 计算
        byte[] rawHmac = hmacSha256(salt, sb.toString());
        return Hex.encodeHexString(rawHmac);
    }

    @SneakyThrows
    public static String sign(HmacPayload info, byte[] payload) {
        Objects.requireNonNull(info, "info 不能为空");
        String salt = Objects.requireNonNull(info.getSalt(), "salt 不能为空");
        // 1. 初始化 Mac
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(salt.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));

        // 2. 传入固定字段
        if (info.getNonce() != null) {
            mac.update(info.getNonce().getBytes(StandardCharsets.UTF_8));
        }
        if (info.getChallenge() != null) {
            mac.update(info.getChallenge().getBytes(StandardCharsets.UTF_8));
        }
        if (info.getTimestamp() != null) {
            mac.update(info.getTimestamp().toString().getBytes(StandardCharsets.UTF_8));
        }

        // 3. 传入二进制负载
        if (payload != null && payload.length > 0) {
            mac.update(payload);
        }

        // 4. 完成并返回 Hex 签名
        byte[] rawHmac = mac.doFinal();
        return Hex.encodeHexString(rawHmac);
    }


    /**
     * 常量时间比较校验签名，防止时序攻击
     *
     * @param providedSignature 客户端提交的 Hex 签名
     * @param info              与签名对应的 BaseHmacInfo
     * @param extras            同 sign 方法的可变参数顺序与值
     */
    @SneakyThrows
    public static boolean verify(String providedSignature, HmacPayload info, Object... extras) {
        String expected = sign(info, extras);
        byte[] a = Hex.decodeHex(expected);
        byte[] b = Hex.decodeHex(providedSignature);
        return MessageDigest.isEqual(a, b);
    }


    /** 计算 HMAC-SHA256 */
    private static byte[] hmacSha256(String key, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 计算失败", e);
        }
    }

    /** null 转空串 */
    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
