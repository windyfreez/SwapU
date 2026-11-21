package com.itsean.swapu_admin.interceptor;

import com.itsean.common.context.BaseContext;
import com.itsean.swapu_admin.annotation.RequirePermission;
import com.itsean.swapu_admin.constant.AdminPermission;
import com.itsean.swapu_admin.constant.ErrorCodeConstant;
import com.itsean.swapu_admin.exception.AdminException;
import com.itsean.swapu_admin.service.AdminPermissionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 权限拦截器测试：注解解析（方法优先/类兜底）、放行规则、403 与请求内缓存
 */
@ExtendWith(MockitoExtension.class)
class AdminPermissionInterceptorTest {

    @Mock
    private AdminPermissionService adminPermissionService;

    @InjectMocks
    private AdminPermissionInterceptor adminPermissionInterceptor;

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    /** 没有任何权限注解的接口 */
    static class OpenController {
        public void open() {
        }
    }

    /** 类上声明 AUDIT，方法上可覆盖为 ADMIN_MANAGE */
    @RequirePermission(AdminPermission.AUDIT)
    static class MixedController {
        @RequirePermission(AdminPermission.ADMIN_MANAGE)
        public void adminOnly() {
        }

        public void audit() {
        }
    }

    private HandlerMethod handlerOf(Class<?> type, String methodName) throws Exception {
        Object bean = type.getDeclaredConstructor().newInstance();
        return new HandlerMethod(bean, type.getMethod(methodName));
    }

    @Test
    @DisplayName("未标注权限的接口直接放行，且不查询权限")
    void openEndpointPassesWithoutQuery() throws Exception {
        boolean pass = adminPermissionInterceptor.preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(),
                handlerOf(OpenController.class, "open"));

        assertTrue(pass);
        verifyNoInteractions(adminPermissionService);
    }

    @Test
    @DisplayName("类级注解生效：拥有 AUDIT 时放行")
    void classLevelAnnotationAllowed() throws Exception {
        when(adminPermissionService.currentPermissions()).thenReturn(EnumSet.of(AdminPermission.AUDIT));

        boolean pass = adminPermissionInterceptor.preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(),
                handlerOf(MixedController.class, "audit"));

        assertTrue(pass);
    }

    @Test
    @DisplayName("方法级注解优先于类级：只有 AUDIT 不能调 ADMIN_MANAGE 接口")
    void methodLevelOverridesClassLevel() throws Exception {
        when(adminPermissionService.currentPermissions()).thenReturn(EnumSet.of(AdminPermission.AUDIT));

        AdminException ex = assertThrows(AdminException.class, () -> adminPermissionInterceptor.preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(),
                handlerOf(MixedController.class, "adminOnly")));

        assertEquals(ErrorCodeConstant.FORBIDDEN, ex.getCode());
    }

    @Test
    @DisplayName("同一请求内权限只查询一次")
    void permissionsCachedWithinSameRequest() throws Exception {
        when(adminPermissionService.currentPermissions()).thenReturn(EnumSet.of(AdminPermission.AUDIT));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = handlerOf(MixedController.class, "audit");

        adminPermissionInterceptor.preHandle(request, response, handler);
        adminPermissionInterceptor.preHandle(request, response, handler);

        verify(adminPermissionService, times(1)).currentPermissions();
    }

    @Test
    @DisplayName("未登录（无 adminId）时由权限服务抛出 403")
    void anonymousDeniedByService() throws Exception {
        AdminException denied = new AdminException("无权限", ErrorCodeConstant.FORBIDDEN);
        when(adminPermissionService.currentPermissions()).thenThrow(denied);

        AdminException ex = assertThrows(AdminException.class, () -> adminPermissionInterceptor.preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(),
                handlerOf(MixedController.class, "audit")));

        assertEquals(ErrorCodeConstant.FORBIDDEN, ex.getCode());
        assertEquals(null, BaseContext.getCurrentId());
    }
}
