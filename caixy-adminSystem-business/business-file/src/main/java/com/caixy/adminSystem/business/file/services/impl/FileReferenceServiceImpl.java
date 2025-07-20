package com.caixy.adminSystem.business.file.services.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caixy.adminSystem.business.file.domain.entity.FileInfo;
import com.caixy.adminSystem.business.file.domain.entity.FileReference;
import com.caixy.adminSystem.business.file.infrastructure.mapper.FileReferenceMapper;
import com.caixy.adminSystem.business.file.services.FileInfoService;
import com.caixy.adminSystem.business.file.services.FileReferenceService;
import com.caixy.adminSystem.common.api.file.dto.FileReferenceDTO;
import com.caixy.adminSystem.common.api.file.dto.FileUploadAfterActionResult;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.exception.ThrowUtils;
import com.caixy.adminSystem.common.base.exception.ErrorCode;
import com.caixy.adminSystem.common.api.file.enums.FileActionBizEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CAIXYPROMISE
 * @description 针对表【t_file_reference(文件引用表)】的数据库操作Service实现
 * @createDate 2025-04-22 19:15:08
 */
@Service
@RequiredArgsConstructor
public class FileReferenceServiceImpl extends ServiceImpl<FileReferenceMapper, FileReference> implements FileReferenceService
{
    private final FileInfoService fileInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileReference bindFileReference(Long fileId, Long userId, FileActionBizEnum bizEnum, FileUploadAfterActionResult after)
    {
        ThrowUtils.throwIf(fileId == null || userId == null
                                   || bizEnum == null || after == null,
                           ErrorCode.PARAMS_ERROR);

        FileInfo fileInfo = fileInfoService.getById(fileId);
        ThrowUtils.throwIf(fileInfo == null,
                           ErrorCode.NOT_FOUND_ERROR, "文件不存在");

        FileReference entity = FileReference.builder()
                                            .fileId(fileId)
                                            .userId(userId)
                                            .bizType(bizEnum.getLabel())
                                            .bizId(after.getBizId())
                                            .displayName(after.getDisplayName())
                                            .accessLevel(after.getAccessLevelEnum().getCode())
                                            .visitUrl(after.getVisitUrl())
                                            .build();

        LambdaQueryWrapper<FileReference> uq =
                new LambdaQueryWrapper<FileReference>()
                        .eq(FileReference::getUserId, userId)
                        .eq(FileReference::getBizType, bizEnum.getLabel())
                        .eq(FileReference::getBizId, after.getBizId())
                        .last("LIMIT 1");

        FileReference old = this.getOne(uq, false);
        boolean ok;

        if (old == null) {
            ok = this.save(entity);
        } else {
            entity.setId(old.getId());
            ok = this.updateById(entity);
        }

        if (!ok) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件引用保存失败");
        }
        return entity;
    }


    /**
     * 根据文件id删除，用于强关联场景
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/4/23 3:14
     */
    @Override
    public Boolean removeFileReferenceById(Long fileId, Long userId, String bizType, Long bizId)
    {
        return this.remove(buildQueryWrapper(fileId, userId, bizType, bizId));
    }

    /**
     * 按 fileId 列表批量删除引用
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/4/23 4:16
     */
    @Override
    public Boolean removeFileReferencesByFileIds(List<Long> fileIds, Long userId, String bizType, Long bizId)
    {
        if (fileIds == null || fileIds.isEmpty())
        {
            return false;
        }

        return this
                .lambdaUpdate()
                .eq(FileReference::getUserId, userId)
                .eq(FileReference::getBizType, bizType)
                .eq(FileReference::getBizId, bizId)
                .in(FileReference::getFileId, fileIds)
                .remove();
    }


    /**
     * 返回用户在指定业务+id下关联的文件。
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/4/23 3:25
     */
    @Override
    public List<FileReferenceDTO> listFileReferenceByBiz(Long userId, String bizType, Long bizId)
    {
        this.lambdaQuery()
                .eq(FileReference::getUserId, userId)
                .eq(FileReference::getBizType, bizType)
                .eq(FileReference::getBizId, bizId)
                .eq(FileReference::getIsDeleted, 0)
                .list();
        return new ArrayList<>();
    }

    @Override
    public FileReference findFileReferenceByBiz(Long userId, String bizType, Long bizId)
    {
        return this
                .lambdaQuery()
                .eq(FileReference::getUserId, userId)
                .eq(FileReference::getBizType, bizType)
                .eq(FileReference::getBizId, bizId)
                .eq(FileReference::getIsDeleted, 0)
                .one();
    }


    private LambdaQueryWrapper<FileReference> buildQueryWrapper(Long fileId, Long userId, String bizType, Long bizId)
    {
        LambdaQueryWrapper<FileReference> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(fileId != null, FileReference::getFileId, fileId)
                .eq(FileReference::getUserId, userId)
                .eq(FileReference::getBizType, bizType)
                .eq(FileReference::getBizId, bizId);
        return queryWrapper;
    }

    @Override
    public Boolean isSameFile(String bizType, Long bizId, Long userId, String sha256, Long fileSize) {
        // 先查出是否存在文件
        FileReference oldRef = this.findFileReferenceByBiz(userId, bizType, bizId);
        if (oldRef == null) {
            return false;
        }
        // 根据文件id比较sha256
        FileInfo fileInfo = fileInfoService.getById(oldRef.getFileId());
        if (fileInfo == null) {
            return false;
        }
        return fileInfo.getFileSha256().equals(sha256) && fileInfo.getFileSize().equals(fileSize);
    }
}




