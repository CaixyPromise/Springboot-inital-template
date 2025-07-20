package com.caixy.adminSystem.infrastructure.file.domain.dto;


import com.caixy.adminSystem.common.base.utils.FileUtils;
import com.caixy.adminSystem.common.api.file.enums.FileActionBizEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadFileDTO
{

    /**
     * 文件信息表信息
     */
//    private FileInfo fileInfo;

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
    private FileSaveInfo fileSaveInfo;

    /**
     * 文件MD5值
     */
    private String sha256;

    /**
     * 文件大小限制，单位：字节
     */
    private Long fileSize;

    /**
     * 转换并生成文件信息
     *
     * @return 构建的 FileInfo 对象
     */
    public FileSaveInfo extractFileInfo() throws IOException
    {
        String uuid = UUID.randomUUID().toString();
        String originalFilename = FileUtils.sanitizeFileName(multipartFile.getOriginalFilename());
        String filename = uuid + FileUtils.desensitizeFileName(originalFilename);

        // 获取文件扩展名称
        String fileSuffix = FileUtils.getSuffix(originalFilename);

        // 使用 FileActionBizEnum 枚举类中的方法生成路径和URL
        Path filePath = fileActionBizEnum.buildFilePath(userId);
        Tika tika = new Tika();
        String mimeType = tika.detect(multipartFile.getInputStream());
        return FileSaveInfo.builder()
                           .uuid(uuid)
                           .fileRealName(originalFilename)
                           .fileInnerName(filename)
                           .filePath(filePath)
                           .fileSuffix(fileSuffix)
                           .contentType(mimeType)
                           .build();
    }
}