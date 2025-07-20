package com.caixy.adminSystem.business.file.services.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caixy.adminSystem.business.file.domain.entity.FileInfo;
import com.caixy.adminSystem.business.file.infrastructure.mapper.FileInfoMapper;
import com.caixy.adminSystem.business.file.services.FileInfoService;
import org.springframework.stereotype.Service;

/**
 * @author CAIXYPROMISE
 * @description 针对表【t_file_info(文件信息表)】的数据库操作Service实现
 * @createDate 2025-04-22 19:15:08
 */
@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo> implements FileInfoService
{

    @Override
    // 根据sha256和文件大小查找文件信息
    public FileInfo findFileBySha256AndSize(String sha256, Long fileSize)
    {
        // 创建LambdaQueryWrapper对象
        LambdaQueryWrapper<FileInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件，文件sha256等于传入的sha256
        lambdaQueryWrapper.eq(FileInfo::getFileSha256, sha256);
        // 设置查询条件，文件大小等于传入的文件大小
        lambdaQueryWrapper.eq(FileInfo::getFileSize, fileSize);
        // 返回查询结果
        return this.getOne(lambdaQueryWrapper);
    }
}




