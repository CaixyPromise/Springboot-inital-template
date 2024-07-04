package com.caixy.adminSystem.controller;

import cn.hutool.core.io.FileUtil;
import com.caixy.adminSystem.annotation.UploadMethodTarget;
import com.caixy.adminSystem.common.BaseResponse;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.common.ResultUtils;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.exception.FileUploadActionException;
import com.caixy.adminSystem.model.dto.file.DownloadFileDTO;
import com.caixy.adminSystem.model.dto.file.UploadFileDTO;
import com.caixy.adminSystem.model.dto.file.UploadFileRequest;
import com.caixy.adminSystem.model.entity.User;
import com.caixy.adminSystem.model.enums.FileActionBizEnum;
import com.caixy.adminSystem.model.enums.SaveFileMethodEnum;
import com.caixy.adminSystem.service.FileActionService;
import com.caixy.adminSystem.service.UploadFileService;
import com.caixy.adminSystem.service.UserService;
import com.caixy.adminSystem.utils.FileUtils;
import com.caixy.adminSystem.utils.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
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
    private UserService userService;

    @Resource
    private UploadFileService uploadFileService;

    @Resource
    private List<FileActionService> fileActionService;

    private HashMap<FileActionBizEnum, FileActionService> serviceCache;

    @PostConstruct
    public void initActionService()
    {
        serviceCache =
                SpringContextUtils.getServiceFromAnnotation(fileActionService, UploadMethodTarget.class);
    }

    @PostMapping("/upload")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<String> uploadFile(
            @RequestPart("file") MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest,
            HttpServletRequest request)
    {
        Path savePath = null;
        UploadFileDTO uploadFileDTO = getUploadFileConfig(multipartFile, uploadFileRequest, request);
        FileActionBizEnum uploadBizEnum = uploadFileDTO.getFileActionBizEnum();
        SaveFileMethodEnum saveFileMethod = uploadFileDTO.getFileActionBizEnum().getSaveFileMethod();
        try
        {
            // 获取文件处理类，如果找不到就会直接报错
            FileActionService actionService = getFileActionService(uploadBizEnum);
            boolean doVerifyFileToken = doBeforeFileUploadAction(actionService, uploadFileDTO, uploadFileRequest);
            if (!doVerifyFileToken)
            {
                log.error("{}-验证token：文件上传失败，文件信息：{}, 上传用户Id: {}", saveFileMethod.getDesc(),
                        uploadFileDTO.getFileInfo(),
                        uploadFileDTO.getUserId());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
            }
            savePath = uploadFileService.saveFile(uploadFileDTO);
            boolean doAfterFileUpload =
                    doAfterFileUploadAction(actionService, uploadFileDTO, savePath, uploadFileRequest);
            if (!doAfterFileUpload)
            {
                log.error("{}：文件上传成功，文件路径：{}，但后续处理失败", saveFileMethod.getDesc(), savePath);
                uploadFileService.deleteFile(uploadFileDTO.getFileActionBizEnum(), savePath);

                log.error("{}：文件上传成功，文件路径：{}，后处理失败后，成功删除文件", saveFileMethod.getDesc(), savePath);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传成功，但后续处理失败");
            }
            log.info("{}：文件上传成功，文件路径：{}", saveFileMethod.getDesc(), savePath);
            return ResultUtils.success(uploadFileDTO.getFileInfo().getFileURL());
        }
        catch (FileUploadActionException | BusinessException | IOException e)
        {
            log.error("{}: 文件上传失败，错误信息: {}", saveFileMethod.getDesc(), e.getMessage());
            // 如果 savePath 不为空，则意味着文件已经上传成功，需要删除它
            if (savePath != null)
            {
                uploadFileService.deleteFile(uploadBizEnum,
                        savePath);
                log.info("{}：文件上传失败，删除文件成功，文件路径：{}", saveFileMethod.getDesc(), savePath);
            }
            // 抛出业务异常，以触发事务回滚
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
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
        User loginUser = userService.getLoginUser(request);

        FileActionService fileActionService = getFileActionService(fileActionBizEnum);
        DownloadFileDTO downloadFileDTO = new DownloadFileDTO();
        downloadFileDTO.setFileId(id);
        downloadFileDTO.setFileActionBizEnum(fileActionBizEnum);
        downloadFileDTO.setUserId(loginUser.getId());
        Boolean beforeDownloadAction = fileActionService.doBeforeDownloadAction(downloadFileDTO);
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
                    if (!fileActionService.doAfterDownloadAction(downloadFileDTO))
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
        User loginUser = userService.getLoginUser(request);
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


    /**
     * 获取业务文件上传处理器
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/6/11 下午8:00
     */
    private FileActionService getFileActionService(FileActionBizEnum fileActionBizEnum)
    {
        FileActionService actionService = serviceCache.get(fileActionBizEnum);
        if (actionService == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "暂无该文件对应业务的操作");
        }
        return actionService;
    }

    private boolean doAfterFileUploadAction(FileActionService actionService, UploadFileDTO uploadFileDTO, Path savePath, UploadFileRequest uploadFileRequest) throws IOException
    {
        return actionService.doAfterUploadAction(uploadFileDTO, savePath, uploadFileRequest);
    }

    private boolean doBeforeFileUploadAction(FileActionService actionService, UploadFileDTO uploadFileDTO
            , UploadFileRequest uploadFileRequest)
    {
        return actionService.doBeforeUploadAction(uploadFileDTO, uploadFileRequest);
    }
}
