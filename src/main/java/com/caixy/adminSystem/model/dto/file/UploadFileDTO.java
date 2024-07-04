package com.caixy.adminSystem.model.dto.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.caixy.adminSystem.manager.uploadManager.core.UploadFileMethodManager;
import com.caixy.adminSystem.model.enums.FileActionBizEnum;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

/**
 * 上传文件配置内部信息
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.dto.file.UploadFileDTO
 * @since 2024-05-21 21:53
 **/
@Data
public class UploadFileDTO
{
    private UploadFileMethodManager uploadManager;

    /**
     * 上传人Id
     */
    private Long userId;

    /**
     * 文件传输对象
     */
    private MultipartFile multipartFile;

    /**
     * 文件业务类型
     */
    private FileActionBizEnum fileActionBizEnum;

    /**
     * 文件描述信息
     */
    private FileInfo fileInfo;

    /**
     * 文件MD5值
     */
    private String sha256;

    /**
     * 文件大小限制，单位：字节
     */
    private Long fileSize;

    @Data
    @Builder
    public static class FileInfo
    {
        /**
         * 文件唯一标识
         */
        private String uuid;

        /**
         * 文件内部名称
         */
        private String fileInnerName;

        /**
         * 文件真实名称
         */
        private String fileRealName;

        /**
         * 文件扩展名称
         */
        private String fileSuffix;

        /**
         * 文件保存路径+名字
         */
        private Path fileAbsolutePathAndName;

        /**
         * 文件保存文件夹路径
         */
        private Path filePath;

        /**
         * 文件可访问路径
         */
        private String fileURL;
    }

    /**
     * 转换并生成文件信息
     *
     * @return 构建的 FileInfo 对象
     */
    public FileInfo convertFileInfo()
    {
        String uuid = UUID.randomUUID().toString();
        String originalFilename = multipartFile.getOriginalFilename();
        String filename = uuid + "-" + DigestUtil.md5Hex(originalFilename + RandomStringUtils.randomAlphanumeric(5));

        // 获取文件扩展名称
        String fileSuffix = FileUtil.getSuffix(originalFilename);

        // 使用 FileActionBizEnum 枚举类中的方法生成路径和URL
        Path fileAbsoluteName = fileActionBizEnum.buildFileAbsolutePathAndName(userId, filename);
        String fileURL = fileActionBizEnum.buildFileURL(userId, filename);
        Path filePath = fileActionBizEnum.buildFilePath(userId);

        return FileInfo.builder()
                .uuid(uuid)
                .fileRealName(originalFilename)
                .fileInnerName(filename)
                .fileAbsolutePathAndName(fileAbsoluteName)
                .filePath(filePath)
                .fileURL(fileURL)
                .fileSuffix(fileSuffix)
                .build();
    }
}