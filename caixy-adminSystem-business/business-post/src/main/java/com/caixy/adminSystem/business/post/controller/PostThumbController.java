package com.caixy.adminSystem.business.post.controller;

import com.caixy.adminSystem.authorization.service.manager.AuthManager;
import com.caixy.adminSystem.business.post.domain.postthumb.dto.PostThumbAddRequest;
import com.caixy.adminSystem.business.post.services.PostThumbService;
import com.caixy.adminSystem.common.api.user.vo.UserVO;
import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.response.ErrorCode;
import com.caixy.adminSystem.common.base.response.Result;
import com.caixy.adminSystem.common.base.response.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子点赞接口
 */
@RestController
@RequestMapping("/post_thumb")
@Slf4j
public class PostThumbController
{

    @Resource
    private PostThumbService postThumbService;


    @Resource
    private AuthManager authManager;

    /**
     * 点赞 / 取消点赞
     *
     * @param postThumbAddRequest
     * @param request
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("/")
    public Result<Integer> doThumb(@RequestBody PostThumbAddRequest postThumbAddRequest,
                                   HttpServletRequest request)
    {
        if (postThumbAddRequest == null || postThumbAddRequest.getPostId() <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final UserVO loginUser = authManager.getLoginUser();
        long postId = postThumbAddRequest.getPostId();
        int result = postThumbService.doPostThumb(postId, loginUser);
        return ResultUtils.success(result);
    }

}
