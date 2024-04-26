package com.caixy.adminSystem;

import org.junit.jupiter.api.Test;

/**
 * 测试密码
 *
 * @name: com.caixy.adminSystem.TestPassword
 * @author: CAIXYPROMISE
 * @since: 2024-04-26 20:29
 **/
public class TestPassword
{
    @Test
    public void test()
    {
        String pattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,20}$";
        String[] passwordList = {"as123456789..", "as123456789", "as123456789.", "as123456789", "as123456789",
                "as123456789", "as123456789", "12391283012u90aslkdajsd0-18923", "as123", "As123456789？`·."};
        for (String password : passwordList) {
            System.out.println(password + ": " + password.matches(pattern));
        }
    }
}
