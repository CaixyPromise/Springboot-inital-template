package com.caixy.adminSystem.controller;

import cn.hutool.core.io.FileUtil;
import com.caixy.adminSystem.common.BaseResponse;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.common.ResultUtils;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.model.dto.file.UploadFileConfig;
import com.caixy.adminSystem.model.dto.file.UploadFileRequest;
import com.caixy.adminSystem.model.entity.User;
import com.caixy.adminSystem.model.enums.FileUploadBizEnum;
import com.caixy.adminSystem.service.FileService;
import com.caixy.adminSystem.service.FileUploadActionService;
import com.caixy.adminSystem.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    private FileService fileService;

    @Resource
    private List<FileUploadActionService> fileUploadActionService;


    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/cos")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           UploadFileRequest uploadFileRequest,
                                           HttpServletRequest request)
    {
        UploadFileConfig uploadFileConfig = getUploadFileConfig(multipartFile, uploadFileRequest, request);
        String savePath = fileService.saveFileToCos(uploadFileConfig);
        boolean doAfterFileUpload = doAfterFileUpload(uploadFileConfig, savePath);
        if (!doAfterFileUpload)
        {
            log.error("COS对象存储：文件上传成功，文件路径：{}，但后续处理失败", savePath);
            fileService.deleteFileOnCos(savePath);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传成功，但后续处理失败");
        }

        log.info("COS对象存储：文件上传成功，文件路径：{}", savePath);
        return ResultUtils.success(savePath);
    }


    /**
     * 保存文件到本地
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/5/21 下午10:36
     */
    @PostMapping("/upload/local")
    public BaseResponse<String> uploadFileToLocal(@RequestPart("file") MultipartFile multipartFile,
                                                  UploadFileRequest uploadFileRequest,
                                                  HttpServletRequest request)
    {
        UploadFileConfig uploadFileConfig = getUploadFileConfig(multipartFile, uploadFileRequest, request);
        String savePath = fileService.saveFileToLocal(uploadFileConfig);
        log.info("本地存储：文件上传成功，文件路径：{}", savePath);
        boolean doAfterFileUpload = doAfterFileUpload(uploadFileConfig, savePath);
        if (!doAfterFileUpload)
        {
            log.error("本地文件存储：文件上传成功，文件路径：{}，但后续处理失败", savePath);
            fileService.deleteFileOnCos(savePath);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传成功，但后续处理失败");
        }
        return ResultUtils.success(uploadFileConfig.getFileInfo().getFileURL());
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     */
    private FileUploadBizEnum validFile(MultipartFile multipartFile, UploadFileRequest uploadFileRequest)
    {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());

        Set<String> acceptFileSuffixList = fileUploadBizEnum.getFileSuffix();
        if (!acceptFileSuffixList.contains(fileSuffix))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件格式不正确");
        }
        boolean lessThanOrEqualTo = fileUploadBizEnum.getMaxSize().isLessThanOrEqualTo(fileSize);
        if (!lessThanOrEqualTo)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小超过限制");
        }
        return fileUploadBizEnum;
    }

    /**
     * 获取上传文件配置信息
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/5/21 下午10:54
     */
    private UploadFileConfig getUploadFileConfig(MultipartFile multipartFile,
                                                 UploadFileRequest uploadFileRequest,
                                                 HttpServletRequest request)
    {
        FileUploadBizEnum fileUploadBizEnum = validFile(multipartFile, uploadFileRequest);
        User loginUser = userService.getLoginUser(request);
        UploadFileConfig uploadFileConfig = new UploadFileConfig();
        uploadFileConfig.setFileUploadBizEnum(fileUploadBizEnum);
        uploadFileConfig.setMultipartFile(multipartFile);
        uploadFileConfig.setUserId(loginUser.getId());
        return uploadFileConfig;
    }

    private boolean doAfterFileUpload(UploadFileConfig uploadFileConfig, String savePath)
    {
        FileUploadBizEnum fileUploadBizEnum = uploadFileConfig.getFileUploadBizEnum();
        String biz = fileUploadBizEnum.getValue();
        FileUploadActionService actionService = fileUploadActionService.stream().filter(fileUploadAction ->
                {
                    Qualifier qualifierAnnotation = fileUploadAction.getClass().getAnnotation(Qualifier.class);
                    if (qualifierAnnotation != null)
                    {
                        log.info("qualifierAnnotation.value: {}", qualifierAnnotation.value());
                    }
                    return qualifierAnnotation != null && qualifierAnnotation.value().equals(biz);
                })
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "暂无该文件操作"));
        return actionService.doAfterUpload(uploadFileConfig, savePath);
    }

}
