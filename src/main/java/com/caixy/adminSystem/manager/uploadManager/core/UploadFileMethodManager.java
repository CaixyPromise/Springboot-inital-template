package com.caixy.adminSystem.manager.uploadManager.core;

import com.caixy.adminSystem.model.dto.file.UploadFileDTO;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @name: com.caixy.adminSystem.manager.uploadManager.core.UploadFileMethodManager
 * @description: 上传文件服务接口实现类
 * @author: CAIXYPROMISE
 * @date: 2024-06-21 20:44
 **/
public interface UploadFileMethodManager
{
    /**
     * 保存文件
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/7/2 下午8:18
     */
    Path saveFile(UploadFileDTO uploadFileDTO) throws IOException;

    /**
     * 删除文件
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/7/2 下午8:18
     */
    void deleteFile(Path key) throws IOException;

    /**
     * 获取文件
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/7/2 下午8:18
     */
    Resource getFile(Path key) throws IOException;
}
