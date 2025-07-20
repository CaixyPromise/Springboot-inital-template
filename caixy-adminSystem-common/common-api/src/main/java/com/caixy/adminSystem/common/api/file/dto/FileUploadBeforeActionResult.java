package com.caixy.adminSystem.common.api.file.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 文件上传前处理操作结果
 *
 * @Author CAIXYPROMISE
 * @since 2025/4/23 2:02
 */
@Data
@Builder
public class FileUploadBeforeActionResult
{
    /**
    * 是否成功
    */
    private Boolean success;

    public static FileUploadBeforeActionResult success() {
        return FileUploadBeforeActionResult.builder()
                .success(true)
                .build();
    }

    public static FileUploadBeforeActionResult fail() {
        return FileUploadBeforeActionResult.builder()
                .success(false)
                .build();
    }
}
