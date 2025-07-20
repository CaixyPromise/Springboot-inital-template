package com.caixy.adminSystem.infrastructure.file.strategy;

import com.caixy.adminSystem.infrastructure.file.domain.dto.UploadFileDTO;
import com.caixy.adminSystem.common.api.file.enums.FileActionBizEnum;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @name: com.caixy.adminSystem.strategy.UploadFileMethodStrategy
 * @description: 上传文件服务接口实现类
 * @author: CAIXYPROMISE
 * @date: 2024-06-21 20:44
 **/
public interface UploadFileMethodStrategy
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
     * 删除文件，允许删除失败
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/19 上午2:20
     */
    Boolean deleteFileWithTolerance(Path key);

    /**
     * 获取文件
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/7/2 下午8:18
     */
    Resource getFile(Path key) throws IOException;

    byte[] readSlice(Path key, long offset, int length) throws IOException;

    /**
     * 构建可以直接网络访问的路径
     *
     * @param userId            用户id
     * @param fileName          文件名
     * @param fileActionBizEnum 业务枚举
     * @return 文件访问路径
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/19 上午2:29
     */
    String buildFileURL(Long userId, String fileName, FileActionBizEnum fileActionBizEnum);
}
