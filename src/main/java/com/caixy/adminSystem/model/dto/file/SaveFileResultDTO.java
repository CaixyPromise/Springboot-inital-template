package com.caixy.adminSystem.model.dto.file;

import lombok.Data;

import java.io.File;

/**
 * 保存文件最终结果后内部使用的信息类
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.dto.file.properties.SaveFileResultDTO
 * @since 2024-06-11 18:02
 **/
@Data
public class SaveFileResultDTO
{
    /**
     * 文件对象
     */
    private File fileObject;

    /**
     * 文件保存位置
     */
    private String savePath;

    /**
     * 文件sha256
     */
    private String sha256;
}
