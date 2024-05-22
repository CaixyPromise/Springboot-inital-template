package com.caixy.adminSystem.service.impl;

import com.caixy.adminSystem.model.dto.file.UploadFileConfig;
import com.caixy.adminSystem.model.enums.FileUploadBizEnum;
import com.caixy.adminSystem.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileServiceImplTest
{
    @Resource
    private FileService fileService;

    @Test
    public void testUploadFile()
    {
        UploadFileConfig uploadFileConfig = new UploadFileConfig();
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "testfile.txt",
                "text/plain",
                "This is a test file content".getBytes()
        );
        uploadFileConfig.setMultipartFile(multipartFile);
        uploadFileConfig.setUserId(1L);
        uploadFileConfig.setFileUploadBizEnum(FileUploadBizEnum.USER_AVATAR);

        String result = fileService.saveFileToCos(uploadFileConfig);
        System.out.println(result);
    }
}