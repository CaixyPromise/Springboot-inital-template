package com.caixy.adminSystem.infrastructure.file.domain.dto;

import com.caixy.adminSystem.common.api.file.enums.FileActionBizEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 文件上传请求
 */
@Data
public class UploadFileRequest implements Serializable
{
    /**
     * 业务
     */
    @NotNull
    private FileActionBizEnum biz;

    /**
     * 上传token
     */
    @NotEmpty
    @Size(max = 36) // uuid
    private String token;

    /**
     * 文件名称，仅用在秒传时传递，用于自定义文件名称
     */
    @NotEmpty
    @Size(max = 128)
    private String fileName;


    /**
     * 文件签名，仅用在秒传时传递，用于校验文件是否一致
     */
    private String signature;

    /**
     * 防重放字段
     */
    @NotEmpty
    @Size(max = 36) // uuid长度
    private String nonce;

    /**
     * 上传触发时时间戳（秒）
     */
    @NotNull
    private Long timestamp;


    private static final long serialVersionUID = 1L;
}