package com.caixy.adminSystem.controller;

import cn.hutool.core.io.FileUtil;
import com.caixy.adminSystem.common.Result;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.common.ResultUtils;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.manager.Authorization.AuthManager;
import com.caixy.adminSystem.manager.UploadManager.utils.FileUtils;
import com.caixy.adminSystem.model.dto.file.DownloadFileDTO;
import com.caixy.adminSystem.model.dto.file.UploadFileDTO;
import com.caixy.adminSystem.model.dto.file.UploadFileRequest;
import com.caixy.adminSystem.model.enums.FileActionBizEnum;
import com.caixy.adminSystem.model.enums.SaveFileMethodEnum;
import com.caixy.adminSystem.model.vo.user.UserVO;
import com.caixy.adminSystem.service.UploadFileService;
import com.caixy.adminSystem.strategy.FileActionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

/**
 * 文件接口
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController
{
    @Resource
    private AuthManager authManager;

    @Resource
    private UploadFileService uploadFileService;


    /**
     * 处理上传文件，上传成功则直接返回文件访问路径
     *
     * @author CAIXYPROMISE
     * @version 2.0 fix 事务失效问题，更加符合规范
     * @since 2024/10/19 上午1:33
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(
            @RequestPart("file") MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest,
            HttpServletRequest request)
    {
        UploadFileDTO uploadFileDTO = getUploadFileConfig(multipartFile, uploadFileRequest, request);
        FileActionBizEnum uploadBizEnum = uploadFileDTO.getFileActionBizEnum();
        SaveFileMethodEnum saveFileMethod = uploadFileDTO.getFileActionBizEnum().getSaveFileMethod();
        return ResultUtils.success(uploadFileService.handleUpload(uploadFileRequest, uploadBizEnum, saveFileMethod, uploadFileDTO, request));
    }

    @GetMapping("/download")
    public void downloadFileById(@RequestParam("id") String id,
                                 @RequestParam("bizName") String bizName,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
    {
        FileActionBizEnum fileActionBizEnum = FileActionBizEnum.getEnumByValue(bizName);
        if (fileActionBizEnum == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "业务类型不存在");
        }
        UserVO loginUser = authManager.getLoginUser();

        FileActionStrategy fileActionStrategy = uploadFileService.getFileActionService(fileActionBizEnum);
        DownloadFileDTO downloadFileDTO = new DownloadFileDTO();
        downloadFileDTO.setFileId(id);
        downloadFileDTO.setFileActionBizEnum(fileActionBizEnum);
        downloadFileDTO.setUserId(loginUser.getId());
        Boolean beforeDownloadAction = fileActionStrategy.doBeforeDownloadAction(downloadFileDTO);
        if (!beforeDownloadAction)
        {
            log.error("文件下载操作前处理失败: userId: {}, 下载文件信息：{}", loginUser.getId(), downloadFileDTO);
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "文件下载操作失败");
        }
        Path fileKey = downloadFileDTO.getFilePath();
        try
        {
            if (fileKey == null)
            {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
            }
            else
            {
                org.springframework.core.io.Resource fileResource = uploadFileService.getFile(fileActionBizEnum, fileKey);
                if (fileResource == null)
                {
                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
                }
                else
                {
                    // 设置响应头
                    buildDownloadResponse(downloadFileDTO, response);

                    // 将文件写入响应输出流
                    StreamUtils.copy(fileResource.getInputStream(), response.getOutputStream());
                    response.flushBuffer();

                    // 执行下载后的操作
                    downloadFileDTO.setFileIsExist(true);
                    if (!fileActionStrategy.doAfterDownloadAction(downloadFileDTO))
                    {
                        log.error("文件下载操作后处理失败: userId: {}, 下载文件信息：{}", loginUser.getId(), downloadFileDTO);
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件下载操作失败");
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     */
    private FileActionBizEnum validFile(MultipartFile multipartFile, UploadFileRequest uploadFileRequest)
    {
        String biz = uploadFileRequest.getBiz();
        FileActionBizEnum fileActionBizEnum = FileActionBizEnum.getEnumByValue(biz);
        if (fileActionBizEnum == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());

        Set<String> acceptFileSuffixList = fileActionBizEnum.getFileSuffix();
        if (!acceptFileSuffixList.contains(fileSuffix))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件格式不正确");
        }
        boolean lessThanOrEqualTo = fileActionBizEnum.getMaxSize().isLessThanOrEqualTo(fileSize);
        if (!lessThanOrEqualTo)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小超过限制");
        }
        return fileActionBizEnum;
    }

    /**
     * 获取上传文件配置信息
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/5/21 下午10:54
     */
    private UploadFileDTO getUploadFileConfig(MultipartFile multipartFile,
                                              UploadFileRequest uploadFileRequest,
                                              HttpServletRequest request)
    {
        FileActionBizEnum fileActionBizEnum = validFile(multipartFile, uploadFileRequest);
        UserVO loginUser = authManager.getLoginUser();
        UploadFileDTO uploadFileDTO = new UploadFileDTO();
        uploadFileDTO.setFileActionBizEnum(fileActionBizEnum);
        uploadFileDTO.setMultipartFile(multipartFile);
        uploadFileDTO.setUserId(loginUser.getId());
        uploadFileDTO.setSha256(FileUtils.getMultiPartFileSha256(multipartFile));
        uploadFileDTO.setFileSize(multipartFile.getSize());
        UploadFileDTO.FileInfo fileInfo = uploadFileDTO.convertFileInfo();
        uploadFileDTO.setFileInfo(fileInfo);
        return uploadFileDTO;
    }

    private void buildDownloadResponse(DownloadFileDTO downloadFileDTO, HttpServletResponse response)
    {
        String fileName = downloadFileDTO.getFileRealName();
        response.setContentType("application/octet-stream;charset=UTF-8;filename=" + fileName);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
    }




}
