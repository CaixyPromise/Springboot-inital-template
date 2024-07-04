package com.caixy.adminSystem.model.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单文件附件信息
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.dto.file.UploadFileInfoDTO
 * @since 2024-06-11 00:32
 **/
@Data
public class UploadFileInfoDTO implements Serializable
{
    /**
     * 文件uid
     */
    private String fileUid;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件sha256
     */
    private String fileSha256;

    /**
     * 文件上传token(上传信息时不设置)
     */
    private String token;

    private static final long serialVersionUID = -1L;
}
