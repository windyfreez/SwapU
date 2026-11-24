package com.itsean.campus_second_hand.service.impl;

import com.itsean.common.captcha.CaptchaScene;
import com.itsean.common.captcha.CaptchaService;
import com.itsean.common.constant.MessageConstant;
import com.itsean.common.exception.CaptchaException;
import com.itsean.campus_second_hand.mapper.ProductMapper;
import com.itsean.campus_second_hand.mapper.UserMapper;
import com.itsean.pojo.dto.UserLoginDTO;
import com.itsean.pojo.dto.UserRegisterDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 登录/注册的验证码前置校验测试
 * <p>
 * 重点不是验证码本身，而是「验证码不通过时绝不能碰数据库」这条边界。
 */
@ExtendWith(MockitoExtension.class)
class UserLoginCaptchaTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("登录：验证码错误直接抛出，不查询数据库")
    void loginRejectedByCaptcha() {
        doThrow(new CaptchaException(MessageConstant.CAPTCHA_INCORRECT))
                .when(captchaService).verify(CaptchaScene.USER_LOGIN, "key-1", "bad");

        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setUsername("alice");
        userLoginDTO.setPassword("123456");
        userLoginDTO.setCaptchaKey("key-1");
        userLoginDTO.setCaptchaCode("bad");

        CaptchaException ex = assertThrows(CaptchaException.class,
                () -> userService.login(userLoginDTO));

        assertEquals(MessageConstant.CAPTCHA_INCORRECT, ex.getMessage());
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("注册：验证码过期直接抛出，不做手机号/邮箱/学号查重")
    void registerRejectedByCaptcha() {
        doThrow(new CaptchaException(MessageConstant.CAPTCHA_EXPIRED))
                .when(captchaService).verify(CaptchaScene.USER_REGISTER, "key-2", "old");

        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setStudentId("2024001");
        userRegisterDTO.setUsername("bob");
        userRegisterDTO.setNickname("bob");
        userRegisterDTO.setPassword("123456");
        userRegisterDTO.setPhone("13800000000");
        userRegisterDTO.setEmail("bob@example.com");
        userRegisterDTO.setCollege("计算机学院");
        userRegisterDTO.setCaptchaKey("key-2");
        userRegisterDTO.setCaptchaCode("old");

        CaptchaException ex = assertThrows(CaptchaException.class,
                () -> userService.register(userRegisterDTO));

        assertEquals(MessageConstant.CAPTCHA_EXPIRED, ex.getMessage());
        verifyNoInteractions(userMapper);
    }
}
