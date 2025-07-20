package com.caixy.adminSystem.business.file.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caixy.adminSystem.business.file.domain.entity.FileInfo;
import org.apache.ibatis.annotations.Param;

/**
* @author CAIXYPROMISE
* @description 针对表【t_file_info(文件信息表)】的数据库操作Mapper
* @createDate 2025-04-22 19:15:08
* @Entity com.caixy.shortlink.model.entity.FileInfo
*/
public interface FileInfoMapper extends BaseMapper<FileInfo> {
    FileInfo findNonReferenceFilesBetweenDays(@Param("day") Integer day);
}




