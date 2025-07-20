package com.caixy.adminSystem.business.file.services;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caixy.adminSystem.business.file.domain.entity.FileInfo;

/**
* @author CAIXYPROMISE
* @description 针对表【t_file_info(文件信息表)】的数据库操作Service
* @createDate 2025-04-22 19:15:08
*/
public interface FileInfoService extends IService<FileInfo> {

    /**
     * 判断文件是否存在，sha256+size可以有效防止文件碰撞
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/4/22 19:38
     */
    FileInfo findFileBySha256AndSize(String sha256, Long fileSize);
}
