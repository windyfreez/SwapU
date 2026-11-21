package com.itsean.swapu_admin.constant;

import com.itsean.common.constant.NumberConstant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 角色 → 权限矩阵测试
 * <p>
 * 这是整个 RBAC 的唯一真源，改映射表必须同步改这里的断言。
 */
class AdminPermissionTest {

    @Test
    @DisplayName("超级管理员拥有全部权限")
    void superAdminHasAllPermissions() {
        Set<AdminPermission> permissions = AdminPermission.ofRole(NumberConstant.ADMIN_ROLE_SUPER);
        assertEquals(EnumSet.allOf(AdminPermission.class), permissions);
    }

    @Test
    @DisplayName("运营管理员：审核 + 分类管理 + 用户管理，但不能管理管理员")
    void operationAdminScope() {
        Set<AdminPermission> permissions = AdminPermission.ofRole(NumberConstant.ADMIN_ROLE_OPERATION);

        assertEquals(EnumSet.of(AdminPermission.AUDIT,
                AdminPermission.CATEGORY_MANAGE,
                AdminPermission.USER_MANAGE), permissions);
        assertFalse(permissions.contains(AdminPermission.ADMIN_MANAGE),
                "运营管理员不应具备管理员管理权限");
    }

    @Test
    @DisplayName("普通管理员只有审核权限")
    void normalAdminOnlyAudit() {
        Set<AdminPermission> permissions = AdminPermission.ofRole(NumberConstant.ADMIN_ROLE_NORMAL);

        assertEquals(EnumSet.of(AdminPermission.AUDIT), permissions);
        assertFalse(permissions.contains(AdminPermission.CATEGORY_MANAGE));
        assertFalse(permissions.contains(AdminPermission.USER_MANAGE));
        assertFalse(permissions.contains(AdminPermission.ADMIN_MANAGE));
    }

    @Test
    @DisplayName("未知角色一律返回空权限，不能多给")
    void unknownRoleGetsNothing() {
        assertTrue(AdminPermission.ofRole(null).isEmpty());
        assertTrue(AdminPermission.ofRole(0).isEmpty());
        assertTrue(AdminPermission.ofRole(4).isEmpty());
        assertTrue(AdminPermission.ofRole(99).isEmpty());
    }

    @Test
    @DisplayName("角色取值合法性校验只认 1/2/3")
    void knownRoleCheck() {
        assertTrue(AdminPermission.isKnownRole(NumberConstant.ADMIN_ROLE_SUPER));
        assertTrue(AdminPermission.isKnownRole(NumberConstant.ADMIN_ROLE_NORMAL));
        assertTrue(AdminPermission.isKnownRole(NumberConstant.ADMIN_ROLE_OPERATION));

        assertFalse(AdminPermission.isKnownRole(null));
        assertFalse(AdminPermission.isKnownRole(0));
        assertFalse(AdminPermission.isKnownRole(4));
    }
}
