package com.caixy.adminSystem.model.dto.file;

import com.caixy.adminSystem.model.enums.FileActionBizEnum;
import lombok.Data;

import java.io.Serializable;
import java.nio.file.Path;

/**
 * 下载文件信息传输类
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.dto.file.DownloadFileDTO
 * @since 2024-07-02 17:03
 **/
@Data
public class DownloadFileDTO implements Serializable
{
    /**
     * 下载文件名称
     */
    private String fileId;
    /**
     * 下载业务名称
     */
    private FileActionBizEnum fileActionBizEnum;
    
    /**
     * 文件内部名称，用于给前处理类设置，交给FileController去识别文件并返回给前端
     */
    private Path filePath;

    /**
     * 用于返回给用户的文件名称
     */
    private String fileRealName;
    
    /**
     * 用户id
     */
    private Long userId;
    
    /**
     * 文件在内部是存在的
     */
    private Boolean fileIsExist;
    

    private static final long serialVersionUID = 1L;
}
