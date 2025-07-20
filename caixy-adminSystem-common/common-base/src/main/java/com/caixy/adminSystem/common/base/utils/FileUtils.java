package com.caixy.adminSystem.common.base.utils;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 文件工具类
 *
 * @Author CAIXYPROMISE
 * @since 2025/5/26 19:53
 */
public class FileUtils extends cn.hutool.core.io.FileUtil
{
    // 最大文件名长度
    private static final int MAX_FILE_NAME_LENGTH = 100;

    private static final Pattern SANITIZE_PATTERN  = Pattern.compile("[\\\\/:*?\"<>|]");

    /**
     * 计算 HMAC-SHA256 文件签名
     * @param fileBytes 文件字节流
     * @param salt salt 作为 HMAC key
     * @return 十六进制字符串签名
     * @throws Exception
     */
    public static String hmacSha256File(byte[] fileBytes, String salt) throws IOException
    {
        HMac hMac = new HMac(HmacAlgorithm.HmacSHA256, salt.getBytes());
        return hMac.digestHex(fileBytes);
    }

    /**
     * 文件名安全化（替换非法字符）
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        return SANITIZE_PATTERN.matcher(fileName).replaceAll("_");
    }
    /**
     * 文件名脱敏：MD5 哈希化 + 安全化 + 长度限制
     */
    public static String desensitizeFileName(String originalFileName)
    {
        if (StringUtils.isBlank(originalFileName))
        {
            throw new IllegalArgumentException("文件名不能为空");
        }
        String uuid = UUID.randomUUID().toString();

        // 提取文件扩展名
        int dotIndex = originalFileName.lastIndexOf(".");
        String extension = "";
        if (dotIndex != -1)
        {
            extension = originalFileName.substring(dotIndex);
            originalFileName = originalFileName.substring(0, dotIndex);
        }

        // 安全化文件名
        String safeFileName = uuid + sanitizeFileName(originalFileName);

        // 哈希处理
        String hashPart = DigestUtils.md5Hex(safeFileName.getBytes(StandardCharsets.UTF_8));

        // 计算允许的哈希长度
        int allowedLength = MAX_FILE_NAME_LENGTH - extension.length();
        if (hashPart.length() > allowedLength)
        {
            hashPart = hashPart.substring(0, allowedLength);
        }

        return hashPart + extension;
    }

    /**
     * 获取文件的SHA-256哈希值
     *
     * @param file 文件对象
     * @return 文件的SHA-256哈希值，以十六进制字符串形式返回
     * @throws IOException
     */
    public static String getFileSha256(File file)
    {
        try (FileInputStream fileInputStream = new FileInputStream(file))
        {
            return calculateSHA256(fileInputStream);
        }
        catch (IOException e)
        {
            throw new RuntimeException("计算文件SHA-256时发生错误", e);
        }
    }

    /**
     * 获取MultipartFile的SHA-256哈希值
     *
     * @param multipartFile 文件对象
     * @return 文件的SHA-256哈希值，以十六进制字符串形式返回
     * @throws IOException
     */
    public static String getMultiPartFileSha256(MultipartFile multipartFile)
    {
        try (InputStream inputStream = multipartFile.getInputStream())
        {
            return calculateSHA256(inputStream);
        }
        catch (IOException e)
        {
            throw new RuntimeException("计算MultipartFile的SHA-256时发生错误", e);
        }
    }

    /**
     * 计算输入流的SHA-256哈希值
     *
     * @param inputStream 输入流
     * @return SHA-256哈希值，以十六进制字符串形式返回
     * @throws IOException
     */
    private static String calculateSHA256(InputStream inputStream) throws IOException
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] byteBuffer = new byte[1024];
            int bytesCount;
            while ((bytesCount = inputStream.read(byteBuffer)) != -1)
            {
                digest.update(byteBuffer, 0, bytesCount);
            }
            byte[] fileBytes = digest.digest();
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : fileBytes)
            {
                stringBuilder.append(String.format("%02x", b));
            }
            return stringBuilder.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("SHA-256 算法不存在", e);
        }
    }
}
