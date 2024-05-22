package com.caixy.adminSystem.model.vo.user;

import com.caixy.adminSystem.constant.RegexPatternConstants;
import com.caixy.adminSystem.model.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AboutMeVOTest
{
    @Test
    public void Test()
    {
        String email = "1944630344@qq.com";
        String phone = "13307629178";

        System.out.println(email.replaceAll(RegexPatternConstants.EMAIL_ENCRYPT_REGEX, "$1****$2"));

        System.out.println(phone.replaceAll(RegexPatternConstants.PHONE_ENCRYPT_REGEX, "$1****$2"));
    }
}