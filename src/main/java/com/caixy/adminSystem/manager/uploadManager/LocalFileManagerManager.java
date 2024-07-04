package com.caixy.adminSystem.manager.uploadManager;

import com.caixy.adminSystem.annotation.UploadMethodTarget;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.config.LocalFileConfig;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.manager.uploadManager.core.UploadFileMethodManager;
import com.caixy.adminSystem.model.dto.file.UploadFileDTO;
import com.caixy.adminSystem.model.enums.FileActionBizEnum;
import com.caixy.adminSystem.model.enums.SaveFileMethodEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件管理器
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.uploadManager.LocalFileManagerManager
 * @since 2024-05-21 20:13
 **/
@Component
@AllArgsConstructor
@Slf4j
@UploadMethodTarget(SaveFileMethodEnum.LOCAL_SAVE)
public class LocalFileManagerManager implements UploadFileMethodManager
{
    private final LocalFileConfig localFileConfig;

    public Path saveFile(MultipartFile multipartFile, UploadFileDTO fileConfig)
    {
        UploadFileDTO.FileInfo fileInfo = fileConfig.getFileInfo();
        String filename = fileInfo.getFileInnerName(); // 文件名
        Path filePath = fileInfo.getFilePath();
        FileActionBizEnum fileActionBizEnum = fileConfig.getFileActionBizEnum();
        Long userId = fileConfig.getUserId();

        // 创建完整的文件路径：<root>/<业务名称>/<用户id>
        Path directoryPath = localFileConfig.getRootLocation().resolve(filePath);

        // 检查并创建目录
        File directory = directoryPath.toFile();
        if (!directory.exists() && !directory.mkdirs())
        {
            log.error("create directory error, directory = {}", directory.getPath());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
        // 确保文件保存到：<directoryPath>/<filename>
        File file = new File(directory, filename);
        try
        {
            multipartFile.transferTo(file); // 保存文件到指定位置
            // 返回文件的相对路径：<saveLocation>/<fileActionBizEnum>/<userId>/<filename>
            return localFileConfig.getRootLocation().resolve(
                    Paths.get(fileActionBizEnum.getValue(),
                            String.valueOf(userId),
                            filename));
        }
        catch (Exception e)
        {
            log.error("file upload error, filepath = {}", file.getPath(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }


    @Override
    public Path saveFile(UploadFileDTO uploadFileDTO) throws IOException
    {
        MultipartFile multipartFile = uploadFileDTO.getMultipartFile();
        return saveFile(multipartFile, uploadFileDTO);
    }

    @Override
    public void deleteFile(Path key) throws IOException
    {
        Path path = localFileConfig.getRootLocation().resolve(key);
        File file = path.toFile();
        if (!file.delete())
        {
            throw new IOException("Failed to delete file: " + file.getPath());
        }
    }

    @Override
    public Resource getFile(Path key) throws IOException
    {
        Path finalPath = localFileConfig.getRootLocation().resolve(key);
        File file = finalPath.toFile();
        return new FileSystemResource(file);
    }
}
