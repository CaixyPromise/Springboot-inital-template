package com.caixy.adminSystem.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 文件操作根据类
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.utils.FileUtils
 * @since 2024-06-11 17:32
 **/
public class FileUtils
{
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
