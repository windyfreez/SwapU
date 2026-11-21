package com.itsean.swapu_admin.service.impl;

import com.itsean.common.constant.MessageConstant;
import com.itsean.common.constant.NumberConstant;
import com.itsean.common.constant.StringConstant;
import com.itsean.common.context.BaseContext;
import com.itsean.pojo.entity.Admin;
import com.itsean.swapu_admin.constant.AdminPermission;
import com.itsean.swapu_admin.constant.ErrorCodeConstant;
import com.itsean.swapu_admin.dto.AdminDTO;
import com.itsean.swapu_admin.exception.AdminException;
import com.itsean.swapu_admin.constant.AdminLogAction;
import com.itsean.swapu_admin.mapper.AdminMapper;
import com.itsean.swapu_admin.service.AdminOperationLogService;
import com.itsean.swapu_admin.service.AdminPermissionService;
import com.itsean.swapu_admin.vo.AdminMeVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 管理员管理（批次二）测试：角色校验、逻辑删除、自我保护、/me
 */
@ExtendWith(MockitoExtension.class)
class AdminManageServiceTest {

    private static final Long SUPER_ID = 1L;
    private static final Long NORMAL_ID = 2L;
    private static final Long TARGET_ID = 5L;

    @Mock
    private AdminMapper adminMapper;

    @Mock
    private AdminPermissionService adminPermissionService;

    @Mock
    private AdminOperationLogService adminOperationLogService;

    @InjectMocks
    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(SUPER_ID);
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    private Admin admin(Long id, String username, Integer role, Integer status) {
        Admin admin = new Admin();
        admin.setId(id);
        admin.setUsername(username);
        admin.setRole(role);
        admin.setStatus(status);
        admin.setPassword("$2a$10$abcdefghijklmnopqrstuv");
        return admin;
    }

    private Admin superAdmin() {
        return admin(SUPER_ID, "root", NumberConstant.ADMIN_ROLE_SUPER, NumberConstant.ADMIN_STATUS_ENABLED);
    }

    private AdminDTO dto(Long id, Integer role) {
        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setId(id);
        adminDTO.setUsername("newbie");
        adminDTO.setPassword("123456");
        adminDTO.setRole(role);
        return adminDTO;
    }

    // ---------------- 新增：角色校验 ----------------

    @Test
    @DisplayName("新增管理员时非法角色被拒绝（4106）")
    void addRejectsUnknownRole() {
        when(adminMapper.findById(SUPER_ID)).thenReturn(superAdmin());

        AdminException ex = assertThrows(AdminException.class,
                () -> adminService.addAdmin(dto(null, 99)));

        assertEquals(ErrorCodeConstant.ADMIN_ROLE_ERROR, ex.getCode());
        assertEquals(MessageConstant.ADMIN_ROLE_ERROR, ex.getMessage());
        verify(adminMapper, never()).insert(any(Admin.class));
    }

    @Test
    @DisplayName("新增管理员不传角色时默认为普通管理员")
    void addDefaultsToNormalRole() {
        when(adminMapper.findById(SUPER_ID)).thenReturn(superAdmin());

        adminService.addAdmin(dto(null, null));

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminMapper).insert(captor.capture());
        assertEquals(NumberConstant.ADMIN_ROLE_NORMAL, captor.getValue().getRole());
        assertTrue(captor.getValue().getPassword().startsWith("$2"), "密码必须是 BCrypt 密文");

        // 写操作应留下操作日志
        verify(adminOperationLogService).record(eq(AdminLogAction.CREATE), eq("ADMIN"), any(),
                any(String.class));
    }

    @Test
    @DisplayName("新增管理员时运营管理员角色可正常落库")
    void addOperationAdminAllowed() {
        when(adminMapper.findById(SUPER_ID)).thenReturn(superAdmin());

        adminService.addAdmin(dto(null, NumberConstant.ADMIN_ROLE_OPERATION));

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminMapper).insert(captor.capture());
        assertEquals(NumberConstant.ADMIN_ROLE_OPERATION, captor.getValue().getRole());
    }

    @Test
    @DisplayName("非超级管理员不能新增管理员")
    void addRequiresSuperAdmin() {
        BaseContext.setCurrentId(NORMAL_ID);
        when(adminMapper.findById(NORMAL_ID))
                .thenReturn(admin(NORMAL_ID, "norm", NumberConstant.ADMIN_ROLE_NORMAL, NumberConstant.ADMIN_STATUS_ENABLED));

        AdminException ex = assertThrows(AdminException.class,
                () -> adminService.addAdmin(dto(null, NumberConstant.ADMIN_ROLE_NORMAL)));

        assertEquals(ErrorCodeConstant.UNAUTHORIZED, ex.getCode());
    }

    // ---------------- 修改：角色保护 ----------------

    @Test
    @DisplayName("不允许修改自己的角色")
    void updateCannotChangeOwnRole() {
        when(adminMapper.findById(SUPER_ID)).thenReturn(superAdmin());

        AdminException ex = assertThrows(AdminException.class,
                () -> adminService.update(dto(SUPER_ID, NumberConstant.ADMIN_ROLE_OPERATION)));

        assertEquals(ErrorCodeConstant.ADMIN_CANNOT_OPERATE_SELF, ex.getCode());
        assertEquals(MessageConstant.ADMIN_CANNOT_CHANGE_SELF_ROLE, ex.getMessage());
        verify(adminMapper, never()).update(any(Admin.class));
    }

    @Test
    @DisplayName("把最后一名启用的超级管理员降级为运营管理员被拒绝")
    void updateDemotingLastSuperAdminRejected() {
        when(adminMapper.findById(SUPER_ID)).thenReturn(superAdmin());
        when(adminMapper.findById(TARGET_ID))
                .thenReturn(admin(TARGET_ID, "bob", NumberConstant.ADMIN_ROLE_SUPER, NumberConstant.ADMIN_STATUS_ENABLED));
        when(adminMapper.countByRoleAndStatusExcludeId(TARGET_ID,
                NumberConstant.ADMIN_ROLE_SUPER, NumberConstant.ADMIN_STATUS_ENABLED)).thenReturn(0);

        AdminException ex = assertThrows(AdminException.class,
                () -> adminService.update(dto(TARGET_ID, NumberConstant.ADMIN_ROLE_OPERATION)));

        assertEquals(ErrorCodeConstant.ADMIN_MUST_KEEP_ONE_SUPER, ex.getCode());
    }

    @Test
    @DisplayName("还有其他超级管理员时，允许把某个超管降级为运营管理员")
    void updateDemotingSuperAdminAllowedWhenAnotherExists() {
        when(adminMapper.findById(SUPER_ID)).thenReturn(superAdmin());
        when(adminMapper.findById(TARGET_ID))
                .thenReturn(admin(TARGET_ID, "bob", NumberConstant.ADMIN_ROLE_SUPER, NumberConstant.ADMIN_STATUS_ENABLED));
        when(adminMapper.countByRoleAndStatusExcludeId(TARGET_ID,
                NumberConstant.ADMIN_ROLE_SUPER, NumberConstant.ADMIN_STATUS_ENABLED)).thenReturn(1);

        adminService.update(dto(TARGET_ID, NumberConstant.ADMIN_ROLE_OPERATION));

        verify(adminMapper).update(any(Admin.class));
        verify(adminOperationLogService).record(eq(AdminLogAction.UPDATE), eq("ADMIN"), eq(TARGET_ID),
                any(String.class));
    }

    @Test
    @DisplayName("修改管理员时非法角色被拒绝")
    void updateRejectsUnknownRole() {
        when(adminMapper.findById(SUPER_ID)).thenReturn(superAdmin());
        when(adminMapper.findById(TARGET_ID))
                .thenReturn(admin(TARGET_ID, "bob", NumberConstant.ADMIN_ROLE_NORMAL, NumberConstant.ADMIN_STATUS_ENABLED));

        AdminException ex = assertThrows(AdminException.class,
                () -> adminService.update(dto(TARGET_ID, 42)));

        assertEquals(ErrorCodeConstant.ADMIN_ROLE_ERROR, ex.getCode());
    }

    // ---------------- 删除：逻辑删除 ----------------

    @Test
    @DisplayName("不能删除自己")
    void deleteSelfRejected() {
        when(adminMapper.findById(SUPER_ID)).thenReturn(superAdmin());

        AdminException ex = assertThrows(AdminException.class, () -> adminService.deleteById(SUPER_ID));

        assertEquals(ErrorCodeConstant.ADMIN_CANNOT_OPERATE_SELF, ex.getCode());
        verify(adminMapper, never()).logicDeleteById(any(), any(), any());
    }

    @Test
    @DisplayName("不能删除最后一名启用的超级管理员")
    void deleteLastSuperAdminRejected() {
        when(adminMapper.findById(SUPER_ID)).thenReturn(superAdmin());
        when(adminMapper.findById(TARGET_ID))
                .thenReturn(admin(TARGET_ID, "bob", NumberConstant.ADMIN_ROLE_SUPER, NumberConstant.ADMIN_STATUS_ENABLED));
        when(adminMapper.countByRoleAndStatusExcludeId(TARGET_ID,
                NumberConstant.ADMIN_ROLE_SUPER, NumberConstant.ADMIN_STATUS_ENABLED)).thenReturn(0);

        AdminException ex = assertThrows(AdminException.class, () -> adminService.deleteById(TARGET_ID));

        assertEquals(ErrorCodeConstant.ADMIN_MUST_KEEP_ONE_SUPER, ex.getCode());
    }

    @Test
    @DisplayName("删除普通管理员走逻辑删除，并把账号名改写以释放原名")
    void deleteUsesLogicalDeleteAndReleasesUsername() {
        when(adminMapper.findById(SUPER_ID)).thenReturn(superAdmin());
        when(adminMapper.findById(TARGET_ID))
                .thenReturn(admin(TARGET_ID, "bob", NumberConstant.ADMIN_ROLE_NORMAL, NumberConstant.ADMIN_STATUS_ENABLED));
        when(adminMapper.logicDeleteById(eq(TARGET_ID), eq("bob_del_5"), any(LocalDateTime.class))).thenReturn(1);

        adminService.deleteById(TARGET_ID);

        verify(adminMapper).logicDeleteById(eq(TARGET_ID), eq("bob_del_5"), any(LocalDateTime.class));
        verify(adminOperationLogService).record(eq(AdminLogAction.DELETE), eq("ADMIN"), eq(TARGET_ID),
                any(String.class));
    }

    @Test
    @DisplayName("并发下已被删除（影响行数为 0）时报「管理员不存在」")
    void deleteWhenAlreadyDeleted() {
        when(adminMapper.findById(SUPER_ID)).thenReturn(superAdmin());
        when(adminMapper.findById(TARGET_ID))
                .thenReturn(admin(TARGET_ID, "bob", NumberConstant.ADMIN_ROLE_NORMAL, NumberConstant.ADMIN_STATUS_ENABLED));
        when(adminMapper.logicDeleteById(eq(TARGET_ID), any(), any(LocalDateTime.class))).thenReturn(0);

        AdminException ex = assertThrows(AdminException.class, () -> adminService.deleteById(TARGET_ID));

        assertEquals(MessageConstant.ADMIN_NOT_EXIST, ex.getMessage());
    }

    // ---------------- 详情与 /me ----------------

    @Test
    @DisplayName("普通管理员可以查自己，但不能查别人")
    void getByIdRequiresPermissionForOthers() {
        BaseContext.setCurrentId(NORMAL_ID);
        when(adminMapper.findById(TARGET_ID))
                .thenReturn(admin(TARGET_ID, "bob", NumberConstant.ADMIN_ROLE_NORMAL, NumberConstant.ADMIN_STATUS_ENABLED));
        when(adminPermissionService.hasPermission(AdminPermission.ADMIN_MANAGE)).thenReturn(false);

        AdminException ex = assertThrows(AdminException.class, () -> adminService.getById(TARGET_ID));
        assertEquals(ErrorCodeConstant.FORBIDDEN, ex.getCode());
    }

    @Test
    @DisplayName("普通管理员查自己不需要管理员管理权限")
    void getByIdSelfNeedsNoPermission() {
        BaseContext.setCurrentId(NORMAL_ID);
        when(adminMapper.findById(NORMAL_ID))
                .thenReturn(admin(NORMAL_ID, "norm", NumberConstant.ADMIN_ROLE_NORMAL, NumberConstant.ADMIN_STATUS_ENABLED));

        assertNotNull(adminService.getById(NORMAL_ID));
        verify(adminPermissionService, never()).hasPermission(any());
    }

    @Test
    @DisplayName("/admin/me 返回角色名称与权限点列表")
    void currentAdminReturnsRoleLabelAndPermissions() {
        when(adminMapper.findById(SUPER_ID)).thenReturn(superAdmin());
        when(adminPermissionService.currentPermissions()).thenReturn(EnumSet.allOf(AdminPermission.class));

        AdminMeVO me = adminService.currentAdmin();

        assertEquals(NumberConstant.ADMIN_ROLE_SUPER, me.getRole());
        assertEquals(StringConstant.ADMIN_ROLE_SUPER_DESC, me.getRoleLabel());
        assertEquals(4, me.getPermissions().size());
        assertTrue(me.getPermissions().contains(AdminPermission.ADMIN_MANAGE.name()));
    }

    @Test
    @DisplayName("/admin/me 对运营管理员只返回 3 个权限")
    void currentAdminForOperationRole() {
        BaseContext.setCurrentId(TARGET_ID);
        when(adminMapper.findById(TARGET_ID))
                .thenReturn(admin(TARGET_ID, "ops", NumberConstant.ADMIN_ROLE_OPERATION, NumberConstant.ADMIN_STATUS_ENABLED));
        when(adminPermissionService.currentPermissions())
                .thenReturn(EnumSet.of(AdminPermission.AUDIT,
                        AdminPermission.CATEGORY_MANAGE, AdminPermission.USER_MANAGE));

        AdminMeVO me = adminService.currentAdmin();

        assertEquals(StringConstant.ADMIN_ROLE_OPERATION_DESC, me.getRoleLabel());
        assertEquals(3, me.getPermissions().size());
        assertTrue(me.getPermissions().contains(AdminPermission.USER_MANAGE.name()));
    }
}
