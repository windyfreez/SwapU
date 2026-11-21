package com.itsean.swapu_admin.service.impl;

import com.itsean.common.constant.MessageConstant;
import com.itsean.common.constant.NumberConstant;
import com.itsean.common.context.BaseContext;
import com.itsean.pojo.entity.Admin;
import com.itsean.swapu_admin.constant.AdminPermission;
import com.itsean.swapu_admin.constant.ErrorCodeConstant;
import com.itsean.swapu_admin.exception.AdminException;
import com.itsean.swapu_admin.mapper.AdminMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * 权限服务测试：按当前登录管理员的角色/状态判定权限
 */
@ExtendWith(MockitoExtension.class)
class AdminPermissionServiceImplTest {

    private static final Long ADMIN_ID = 1L;

    @Mock
    private AdminMapper adminMapper;

    @InjectMocks
    private AdminPermissionServiceImpl adminPermissionService;

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    private Admin admin(Integer role, Integer status) {
        Admin admin = new Admin();
        admin.setId(ADMIN_ID);
        admin.setRole(role);
        admin.setStatus(status);
        return admin;
    }

    @Test
    @DisplayName("普通管理员访问用户管理被拒（403）")
    void normalAdminCannotManageUsers() {
        BaseContext.setCurrentId(ADMIN_ID);
        when(adminMapper.findById(ADMIN_ID))
                .thenReturn(admin(NumberConstant.ADMIN_ROLE_NORMAL, NumberConstant.ADMIN_STATUS_ENABLED));

        AdminException ex = assertThrows(AdminException.class,
                () -> adminPermissionService.check(AdminPermission.USER_MANAGE));

        assertEquals(ErrorCodeConstant.FORBIDDEN, ex.getCode());
        assertEquals(MessageConstant.ADMIN_PERMISSION_DENIED, ex.getMessage());
    }

    @Test
    @DisplayName("普通管理员可以审核")
    void normalAdminCanAudit() {
        BaseContext.setCurrentId(ADMIN_ID);
        when(adminMapper.findById(ADMIN_ID))
                .thenReturn(admin(NumberConstant.ADMIN_ROLE_NORMAL, NumberConstant.ADMIN_STATUS_ENABLED));

        assertDoesNotThrow(() -> adminPermissionService.check(AdminPermission.AUDIT));
        assertTrue(adminPermissionService.hasPermission(AdminPermission.AUDIT));
    }

    @Test
    @DisplayName("运营管理员可以管用户/分类，但不能管管理员")
    void operationAdminScope() {
        BaseContext.setCurrentId(ADMIN_ID);
        when(adminMapper.findById(ADMIN_ID))
                .thenReturn(admin(NumberConstant.ADMIN_ROLE_OPERATION, NumberConstant.ADMIN_STATUS_ENABLED));

        assertDoesNotThrow(() -> adminPermissionService.check(AdminPermission.USER_MANAGE));
        assertDoesNotThrow(() -> adminPermissionService.check(AdminPermission.CATEGORY_MANAGE));

        AdminException ex = assertThrows(AdminException.class,
                () -> adminPermissionService.check(AdminPermission.ADMIN_MANAGE));
        assertEquals(ErrorCodeConstant.FORBIDDEN, ex.getCode());
    }

    @Test
    @DisplayName("超级管理员四个权限全部通过")
    void superAdminPassesEverything() {
        BaseContext.setCurrentId(ADMIN_ID);
        when(adminMapper.findById(ADMIN_ID))
                .thenReturn(admin(NumberConstant.ADMIN_ROLE_SUPER, NumberConstant.ADMIN_STATUS_ENABLED));

        for (AdminPermission permission : AdminPermission.values()) {
            assertDoesNotThrow(() -> adminPermissionService.check(permission));
        }
    }

    @Test
    @DisplayName("账号被禁用后立即失去权限（不等 token 过期）")
    void disabledAdminHasNoPermission() {
        BaseContext.setCurrentId(ADMIN_ID);
        when(adminMapper.findById(ADMIN_ID))
                .thenReturn(admin(NumberConstant.ADMIN_ROLE_SUPER, NumberConstant.ADMIN_STATUS_DISABLED));

        AdminException ex = assertThrows(AdminException.class, adminPermissionService::currentPermissions);

        assertEquals(ErrorCodeConstant.ADMIN_ACCOUNT_DISABLED, ex.getCode());
    }

    @Test
    @DisplayName("账号已被删除/查不到时按无权限处理")
    void missingAdminDenied() {
        BaseContext.setCurrentId(ADMIN_ID);
        when(adminMapper.findById(ADMIN_ID)).thenReturn(null);

        AdminException ex = assertThrows(AdminException.class, adminPermissionService::currentPermissions);
        assertEquals(ErrorCodeConstant.FORBIDDEN, ex.getCode());
    }

    @Test
    @DisplayName("没有登录上下文时按无权限处理，不再查库")
    void noLoginContextDenied() {
        AdminException ex = assertThrows(AdminException.class, adminPermissionService::currentPermissions);
        assertEquals(ErrorCodeConstant.FORBIDDEN, ex.getCode());
    }

    @Test
    @DisplayName("未知角色的账号拿不到任何权限")
    void unknownRoleGetsEmptyPermissions() {
        BaseContext.setCurrentId(ADMIN_ID);
        when(adminMapper.findById(ADMIN_ID))
                .thenReturn(admin(9, NumberConstant.ADMIN_STATUS_ENABLED));

        assertTrue(adminPermissionService.currentPermissions().isEmpty());
        assertFalse(adminPermissionService.hasPermission(AdminPermission.AUDIT));
    }
}
