package com.caixy.adminSystem.model.dto.file;

import com.caixy.adminSystem.model.enums.FileUploadBizEnum;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传文件配置内部信息
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.dto.file.UploadFileConfig
 * @since 2024-05-21 21:53
 **/
@Data
public class UploadFileConfig
{
    private Long userId;
    private MultipartFile multipartFile;
    private FileUploadBizEnum fileUploadBizEnum;
    private FileInfo fileInfo;


    @Data
    @Builder
    public static class FileInfo
    {
        /**
         * 用户ID
         */
        private Long userId;
        /**
         * 文件业务类型
         */
        private FileUploadBizEnum fileUploadBizEnum;
        /**
         * 文件唯一标识
         */
        private String uuid;
        /**
         * 文件名
         */
        private String filename;
        /**
         * 文件保存路径
         */
        private String filePath;

        /**
         * 文件可访问路径
         */
        private String fileURL;
    }

    /**
     * 获取文件保存路径(本地或COS)
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/5/21 下午10:03
     */
    public FileInfo convertFileInfo(boolean isLocal)
    {
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        FileInfo.FileInfoBuilder fileInfoBuilder = FileInfo.builder()
                .userId(userId)
                .fileUploadBizEnum(fileUploadBizEnum)
                .uuid(uuid)
                .filename(filename);
        if (isLocal)
        {
            fileInfoBuilder.filePath(String.format("/%s/%s", fileUploadBizEnum.getValue(), userId));
            // 设置访问路径，是routePath+userId+filename
            fileInfoBuilder.fileURL(String.format("/%s/%s/%s", fileUploadBizEnum.getRoutePath(), userId, filename));
        }
        else
        {
            String savePath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), userId, filename);
            fileInfoBuilder.filePath(savePath);
            fileInfoBuilder.fileURL(savePath);
        }
        return fileInfoBuilder.build();
    }
}
