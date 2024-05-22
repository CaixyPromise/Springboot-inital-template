package com.caixy.adminSystem.manager;

import com.caixy.adminSystem.config.LocalFileConfig;
import com.caixy.adminSystem.model.enums.FileUploadBizEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LocalFileManagerTest
{
    @Test
    void test()
    {
        int[] arr = {1, 2, 3, 4, 5};

    }
}