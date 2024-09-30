package com.caixy.adminSystem.service;

import com.caixy.adminSystem.model.entity.User;
import javax.annotation.Resource;

import com.caixy.adminSystem.model.vo.user.UserVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 帖子点赞服务测试
 *
 
 */
@SpringBootTest
class PostThumbServiceTest {

    @Resource
    private PostThumbService postThumbService;

    private static final UserVO loginUser = new UserVO();

    @BeforeAll
    static void setUp() {
        loginUser.setId(1L);
    }

    @Test
    void doPostThumb() {
        int i = postThumbService.doPostThumb(1L, loginUser);
        Assertions.assertTrue(i >= 0);
    }
}
