package com.itsean.swapu_admin.service.impl;

import com.itsean.common.captcha.CaptchaScene;
import com.itsean.common.captcha.CaptchaService;
import com.itsean.common.constant.MessageConstant;
import com.itsean.common.exception.CaptchaException;
import com.itsean.swapu_admin.dto.AdminLoginDTO;
import com.itsean.swapu_admin.mapper.AdminMapper;
import com.itsean.swapu_admin.service.AdminOperationLogService;
import com.itsean.swapu_admin.service.AdminPermissionService;
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
 * 管理端登录的验证码前置校验测试
 */
@ExtendWith(MockitoExtension.class)
class AdminLoginCaptchaTest {

    @Mock
    private AdminMapper adminMapper;

    @Mock
    private AdminPermissionService adminPermissionService;

    @Mock
    private AdminOperationLogService adminOperationLogService;

    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    @DisplayName("验证码过期时直接抛出，不查询管理员账号")
    void loginRejectedByCaptcha() {
        doThrow(new CaptchaException(MessageConstant.CAPTCHA_EXPIRED))
                .when(captchaService).verify(CaptchaScene.ADMIN_LOGIN, "key-9", "old");

        AdminLoginDTO adminLoginDTO = new AdminLoginDTO();
        adminLoginDTO.setUsername("superadmin");
        adminLoginDTO.setPassword("123456");
        adminLoginDTO.setCaptchaKey("key-9");
        adminLoginDTO.setCaptchaCode("old");

        CaptchaException ex = assertThrows(CaptchaException.class,
                () -> adminService.login(adminLoginDTO));

        assertEquals(MessageConstant.CAPTCHA_EXPIRED, ex.getMessage());
        verifyNoInteractions(adminMapper);
    }
}
