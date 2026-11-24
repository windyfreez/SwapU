package com.itsean.campus_second_hand.service.impl;

import com.itsean.campus_second_hand.mapper.ProductMapper;
import com.itsean.campus_second_hand.mapper.UserMapper;
import com.itsean.common.captcha.CaptchaService;
import com.itsean.common.constant.MessageConstant;
import com.itsean.common.constant.NumberConstant;
import com.itsean.common.exception.AccountRepeatException;
import com.itsean.pojo.dto.UserLoginDTO;
import com.itsean.pojo.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.DigestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * 用户登录失败提示测试
 * <p>
 * 需求：登录失败要能区分"用户名或密码错误"与"验证码错误"，不再只有笼统的"登录失败"。
 * 这里锁定前三类：账号不存在、密码错误（两者合并提示）、账号锁定。
 */
@ExtendWith(MockitoExtension.class)
class UserLoginErrorTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private UserServiceImpl userService;

    private UserLoginDTO dto(String username, String password) {
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setUsername(username);
        userLoginDTO.setPassword(password);
        // 验证码通过：CaptchaService 是 Mock，void 方法默认不做任何事
        userLoginDTO.setCaptchaKey("key-1");
        userLoginDTO.setCaptchaCode("A7K2");
        return userLoginDTO;
    }

    @Test
    @DisplayName("账号不存在：提示「用户名或密码错误」，不暴露账号是否存在")
    void userNotFoundUsesUnifiedMessage() {
        when(userMapper.findByUsername("nobody")).thenReturn(null);

        AccountRepeatException ex = assertThrows(AccountRepeatException.class,
                () -> userService.login(dto("nobody", "123456")));

        assertEquals(MessageConstant.LOGIN_CREDENTIALS_ERROR, ex.getMessage());
    }

    @Test
    @DisplayName("密码错误：与账号不存在返回完全相同的提示")
    void wrongPasswordUsesUnifiedMessage() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword(DigestUtils.md5DigestAsHex("correct-password".getBytes()));
        user.setStatus(NumberConstant.ACCOUNT_STATUS_NORMAL);
        when(userMapper.findByUsername("alice")).thenReturn(user);

        AccountRepeatException ex = assertThrows(AccountRepeatException.class,
                () -> userService.login(dto("alice", "wrong-password")));

        assertEquals(MessageConstant.LOGIN_CREDENTIALS_ERROR, ex.getMessage());
    }

    @Test
    @DisplayName("账号被锁定：密码正确时单独提示锁定状态")
    void lockedAccountHasItsOwnMessage() {
        User user = new User();
        user.setId(2L);
        user.setUsername("bob");
        user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        user.setStatus(NumberConstant.ACCOUNT_STATUS_LOCK);
        when(userMapper.findByUsername("bob")).thenReturn(user);

        AccountRepeatException ex = assertThrows(AccountRepeatException.class,
                () -> userService.login(dto("bob", "123456")));

        assertEquals(MessageConstant.USER_LOCK, ex.getMessage());
    }

    @Test
    @DisplayName("账号密码正确且状态正常时返回用户对象")
    void successReturnsUser() {
        User user = new User();
        user.setId(3L);
        user.setUsername("carol");
        user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        user.setStatus(NumberConstant.ACCOUNT_STATUS_NORMAL);
        when(userMapper.findByUsername("carol")).thenReturn(user);

        assertSame(user, userService.login(dto("carol", "123456")));
    }
}
