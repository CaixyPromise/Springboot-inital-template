package com.caixy.adminSystem.common.api.file.dto;

import com.caixy.adminSystem.common.api.file.enums.FileAccessLevelEnum;
import lombok.Builder;
import lombok.Data;

/**
 * 文件上传后处理操作结果
 *
 * @Author CAIXYPROMISE
 * @since 2025/4/23 2:03
 */
@Data
@Builder
public class FileUploadAfterActionResult
{
    /**
     * 是否成功
     */
    private Boolean success;

    /**
    * 文件展示名称
    */
    private String displayName;

    /**
    * 文件访问级别
    */
    private FileAccessLevelEnum accessLevelEnum;

    /**
    * 业务关联信息id
    */
    private Long bizId;

    /**
    * 文件访问url
    */
    private String visitUrl;

    public static FileUploadAfterActionResult success() {
        return FileUploadAfterActionResult.builder()
                .success(true)
                .build();
    }


    public static FileUploadAfterActionResult.FileUploadAfterActionResultBuilder successBuilder() {
        return FileUploadAfterActionResult.builder()
                                          .success(true);
    }

    public static FileUploadAfterActionResult fail() {
        return FileUploadAfterActionResult.builder()
                .success(false)
                .build();
    }
}
