package com.caixy.adminSystem;

import com.caixy.adminSystem.utils.EncryptionUtils;
import com.caixy.adminSystem.utils.RegexUtils;
import org.junit.jupiter.api.Test;

/**
 * 测试密码
 *
 * @name: com.caixy.adminSystem.TestRegex
 * @author: CAIXYPROMISE
 * @since: 2024-04-26 20:29
 **/
public class TestRegex
{
    @Test
    public void test()
    {
        String password = "As123456789";
        String encryptedPassword = "$2a$10$Q8T7GHowRg/TDefThcNQsuB2JtzoBlqV5BLInc0SnlfaoPX4KecYa";
        boolean matches = EncryptionUtils.matches(password, encryptedPassword);
        System.out.println(matches);
    }
}
