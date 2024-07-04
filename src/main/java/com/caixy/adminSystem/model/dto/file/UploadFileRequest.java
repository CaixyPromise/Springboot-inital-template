package com.caixy.adminSystem.model.dto.file;

import lombok.Data;

import javax.validation.constraints.NotNull;
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
    private String biz;

    /**
     * 上传token
     */
    @NotNull
    private String token;

    private static final long serialVersionUID = 1L;
}