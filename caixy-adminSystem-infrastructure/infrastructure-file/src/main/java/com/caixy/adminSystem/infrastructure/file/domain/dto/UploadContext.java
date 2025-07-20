package com.caixy.adminSystem.infrastructure.file.domain.dto;

import com.caixy.adminSystem.common.api.file.dto.FileInfoDTO;
import com.caixy.adminSystem.common.api.file.dto.FileUploadAfterActionResult;
import com.caixy.adminSystem.common.api.file.dto.FileUploadBeforeActionResult;
import com.caixy.adminSystem.common.api.file.facade.FileActionHelper;
import com.caixy.adminSystem.infrastructure.file.strategy.FileActionStrategy;
import com.caixy.adminSystem.infrastructure.file.strategy.UploadFileMethodStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.util.Map;

/**
 * 上传文件上下文信息
 *
 * @Author CAIXYPROMISE
 * @since 2025/5/27 0:03
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadContext
{
    // 输入：来自前端的参数
    /**
     * 上传token
     */
    private String token;
    /**
     * 上传文件请求
     */
    private UploadFileRequest uploadFileRequest;
    /**
     * 请求用户id
     */
    private Long userId;

    /**
     * 请求实体
     */
    private HttpServletRequest request;
    /**
     * 缓存的文件校验信息
     */
    private Map<String, Object> cacheMap;

    // 秒传准备阶段解析后
    /**
     * 文件信息(存储在数据库的)
     */
    private FileInfoDTO fileInfo;

    // 保存阶段产生
    private UploadFileDTO uploadFileDTO;
    /**
     * 保存路径
     */
    private Path savePath;
    /**
     * 访问地址
     */
    private String visitUrl;


    // 业务钩子与业务结果
    /**
     * 文件操作策略类
     */
    private FileActionStrategy fileActionStrategy;

    /**
    * 上传文件方法策略类
    */
    private UploadFileMethodStrategy uploadFileMethodStrategy;

    /**
    * 文件操作助手类，给业务类实现删除或更新逻辑
    */
    private FileActionHelper fileActionHelper;

    /**
     * 文件上传前处理结果
     */
    private FileUploadBeforeActionResult beforeActionResult;

    /**
     * 文件上传后处理结果
     */
    private FileUploadAfterActionResult afterActionResult;


    public static UploadContext fromRequest(
            UploadFileRequest uploadFileRequest,
            HttpServletRequest request,
            Long userId,
            Map<String, Object> fileInfoMap,
            UploadFileDTO uploadFileDTO
            )
    {
        return UploadContext.builder()
                .token(uploadFileRequest.getToken())
                .uploadFileRequest(uploadFileRequest)
                .request(request)
                .userId(userId)
                .cacheMap(fileInfoMap)
                .uploadFileDTO(uploadFileDTO)
                .build();
    }
}
