package com.caixy.adminSystem.utils;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;

/**
 * Aes加解密操作类
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.utils.AesUtils
 * @since 2024-07-16 00:42
 **/
public class AesUtils
{
    private static final String DEFAULT_KEY = "S6osrowRYFmnY5ctNIbCua5kY1p1FrBf7kV9P3unJHU=";

    public static final int DECRYPT_MODE = Cipher.DECRYPT_MODE;
    public static final int ENCRYPT_MODE = Cipher.ENCRYPT_MODE;
    private static final Cipher DEFAULT_DECRYPT_CIPHER;
    private static final Cipher DEFAULT_ENCRYPT_CIPHER;

    static
    {
        try
        {
            DEFAULT_DECRYPT_CIPHER = initDefaultCipher(DECRYPT_MODE);
            DEFAULT_ENCRYPT_CIPHER = initDefaultCipher(ENCRYPT_MODE);

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 初始化默认的加解密Cipher对象。
     * @param opMode 加解密模式，使用Cipher的静态常量指定，如Cipher.ENCRYPT_MODE或Cipher.DECRYPT_MODE。
     * @return 配置好的Cipher实例。
     * @throws Exception 如果初始化失败。
     */
    private static Cipher initDefaultCipher(int opMode) throws Exception {
        return EncryptionMode.ECB.getCipher(DEFAULT_KEY, null, opMode);
    }

    /**
     * 使用默认的Cipher加密文本。
     * @param content 要加密的文本。
     * @return 加密后的文本，以Base64编码表示。
     */
    public static String encrypt(String content) {
        return encrypt(content, DEFAULT_ENCRYPT_CIPHER);
    }

    /**
     * 使用默认的Cipher解密文本。
     * @param content 要解密的文本，以Base64编码表示。
     * @return 解密后的原文。
     */
    public static String decrypt(String content) {
        return decrypt(content, DEFAULT_DECRYPT_CIPHER);
    }
    /**
     * 使用指定的Cipher对象加密文本。
     * @param content 要加密的文本。
     * @param cipher 加密用的Cipher对象。
     * @return 加密后的文本，以Base64编码表示。
     */
    public static String encrypt(String content, Cipher cipher)
    {
        try
        {
            if (StringUtils.isBlank(content))
            {
                return content;
            }
            byte[] encrypted = cipher.doFinal(content.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * 使用指定的Cipher对象解密文本。
     * @param content 要解密的文本，以Base64编码表示。
     * @param cipher 解密用的Cipher对象。
     * @return 解密后的原文。
     */
    public static String decrypt(String content, Cipher cipher)
    {
        try
        {
            if (StringUtils.isBlank(content))
            {
                return content;
            }
            byte[] decoded = Base64.getDecoder().decode(content);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * 根据指定的加密模式生成密钥和可能的初始化向量（IV）。
     * @param mode 指定的加密模式。
     * @return 包含生成的密钥和IV的HashMap，键为"key"和"iv"。
     * @throws Exception 如果生成过程中出现错误。
     */
    public static HashMap<String, String> generate(EncryptionMode mode) throws Exception
    {
        HashMap<String, String> result = new HashMap<>();
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(mode.getKeySize());
        SecretKey secretKey = keyGenerator.generateKey();
        byte[] keyBytes = secretKey.getEncoded();
        result.put("key", Base64.getEncoder().encodeToString(keyBytes));

        if (mode.getIvSize() > 0)
        {
            byte[] ivs = new byte[mode.getIvSize()];
            new SecureRandom().nextBytes(ivs);
            result.put("iv", Base64.getEncoder().encodeToString(ivs));
        }
        return result;
    }

    /**
     * AES加密模式
     * 模式解释
     * <li>CBC (Cipher Block Chaining)：每个明文块在被加密前与前一个密文块进行XOR操作。需要一个随机IV来加密第一个块。</li>
     * <li>GCM (Galois/Counter Mode)：提供认证加密，使用一个nonce（可以视为IV）。</li>
     * <li>ECB (Electronic Codebook)：每个块独立加密，不使用IV。由于其确定性，不推荐用于安全性要求高的环境。</li>
     * <li>CFB (Cipher Feedback)：将块密码转换为自同步的流密码。加密的某些部分可以在传输时被修改。</li>
     * <li>OFB (Output Feedback)：将块密码转换为同步流密码。输出是与明文XOR的密钥流。</li>
     * <li>CTR (Counter Mode)：将块密码转换成流密码。每个块的加密独立于其他块，非常适合并行计算。</li>
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/7/16 上午1:08
     */
    @Getter
    public enum EncryptionMode
    {
        // 使用256位密钥，16字节IV，适用于块加密
        CBC("AES/CBC/PKCS5Padding", 256, 16),
        // 使用256位密钥，12字节IV，适用于流加密和验证
        GCM("AES/GCM/NoPadding", 256, 12),
        // 使用256位密钥，不需要IV
        ECB("AES/ECB/PKCS5Padding", 256, 0),
        // 使用256位密钥，16字节IV，适用于流加密
        CFB("AES/CFB/NoPadding", 256, 16),
        // 使用256位密钥，16字节IV，产生密钥流
        OFB("AES/OFB/NoPadding", 256, 16),
        // 使用256位密钥，16字节IV，适用于计数器模式
        CTR("AES/CTR/NoPadding", 256, 16);

        private final String cipherTransformation;
        private final int keySize;
        private final int ivSize;

        EncryptionMode(String cipherTransformation, int keySize, int ivSize)
        {
            this.cipherTransformation = cipherTransformation;
            this.keySize = keySize;
            this.ivSize = ivSize;
        }
        /**
         * 根据给定的加密模式、密钥、IV和操作模式创建Cipher对象。
         * @param base64Key 密钥的Base64编码字符串。
         * @param base64Iv IV的Base64编码字符串，对于不需要IV的模式可以为null。
         * @param opMode 操作模式，Cipher.ENCRYPT_MODE或Cipher.DECRYPT_MODE。
         * @return 配置好的Cipher对象
         * @throws Exception 如果Cipher初始化失败。
         *
         * @author CAIXYPROMISE
         * @since 2024/7/16 上午1:08
         */
        public Cipher getCipher(String base64Key, String base64Iv, int opMode) throws Exception
        {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            byte[] ivBytes = getIvSize() > 0 ? Base64.getDecoder().decode(base64Iv) : new byte[0];
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(getCipherTransformation());
            if (getIvSize() > 0)
            {
                if (this == EncryptionMode.GCM)
                {
                    // GCMParameterSpec的第一个参数是认证标签的位长度，通常设置为128位
                    GCMParameterSpec gcmSpec = new GCMParameterSpec(128, ivBytes);
                    cipher.init(opMode, keySpec, gcmSpec);
                }
                else
                {
                    IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
                    cipher.init(opMode, keySpec, ivSpec);
                }
            }
            else
            {
                cipher.init(opMode, keySpec);
            }
            return cipher;
        }
    }

    public static void main(String[] args) throws Exception
    {
        // 测试文本
        String testString = "Hello, Encryption World!";
        System.out.println("Original text: " + testString);

        // 测试默认加密和解密
        System.out.println("\nTesting default ECB encryption and decryption:");
        String encryptedDefault = AesUtils.encrypt(testString);
        System.out.println("Encrypted (ECB): " + encryptedDefault);
        String decryptedDefault = AesUtils.decrypt(encryptedDefault);
        System.out.println("Decrypted (ECB): " + decryptedDefault);

        // 测试所有加密模式
        for (AesUtils.EncryptionMode mode : AesUtils.EncryptionMode.values())
        {
            System.out.println("\nTesting " + mode.name() + " encryption and decryption:");

            // 生成密钥和IV
            HashMap<String, String> keys = AesUtils.generate(mode);
            String key = keys.get("key");
            String iv = keys.get("iv");

            // 加密
            Cipher cipher = mode.getCipher(key, iv, Cipher.ENCRYPT_MODE);
            String encrypted = AesUtils.encrypt(testString, cipher);
            System.out.println("Encrypted (" + mode.name() + "): " + encrypted);

            // 解密
            cipher = mode.getCipher(key, iv, Cipher.DECRYPT_MODE);
            String decrypted = AesUtils.decrypt(encrypted, cipher);
            System.out.println("Decrypted (" + mode.name() + "): " + decrypted);
        }
    }
}
