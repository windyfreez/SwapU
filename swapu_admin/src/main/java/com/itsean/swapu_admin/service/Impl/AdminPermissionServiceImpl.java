package com.itsean.swapu_admin.service.impl;

import com.itsean.common.constant.MessageConstant;
import com.itsean.common.constant.NumberConstant;
import com.itsean.common.context.BaseContext;
import com.itsean.pojo.entity.Admin;
import com.itsean.swapu_admin.constant.AdminPermission;
import com.itsean.swapu_admin.constant.ErrorCodeConstant;
import com.itsean.swapu_admin.exception.AdminException;
import com.itsean.swapu_admin.mapper.AdminMapper;
import com.itsean.swapu_admin.service.AdminPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 管理端权限服务实现
 */
@Service
@Slf4j
public class AdminPermissionServiceImpl implements AdminPermissionService {

    @Autowired
    private AdminMapper adminMapper;

    /**
     * 查询当前登录管理员的权限集合
     *
     * @return 权限集合
     */
    @Override
    public Set<AdminPermission> currentPermissions() {
        Long adminId = BaseContext.getCurrentId();

        //没有登录上下文（例如被误加入白名单）一律按无权限处理
        if (adminId == null) {
            throw permissionDenied();
        }

        Admin admin = adminMapper.findById(adminId);
        if (admin == null) {
            log.warn("权限校验失败：管理员不存在，adminId={}", adminId);
            throw permissionDenied();
        }

        //账号被禁用后，已签发的 token 也应立即失效
        if (!NumberConstant.ADMIN_STATUS_ENABLED.equals(admin.getStatus())) {
            log.warn("权限校验失败：管理员已被禁用，adminId={}", adminId);
            throw new AdminException(MessageConstant.ADMIN_ACCOUNT_DISABLED,
                    ErrorCodeConstant.ADMIN_ACCOUNT_DISABLED);
        }

        return AdminPermission.ofRole(admin.getRole());
    }

    /**
     * 判断当前登录管理员是否拥有指定权限
     *
     * @param permission 权限点
     * @return true 拥有
     */
    @Override
    public boolean hasPermission(AdminPermission permission) {
        return currentPermissions().contains(permission);
    }

    /**
     * 校验权限，不拥有则抛出 403 业务异常
     *
     * @param permission 权限点
     */
    @Override
    public void check(AdminPermission permission) {
        if (!hasPermission(permission)) {
            log.warn("权限校验失败：adminId={} 缺少权限 {}", BaseContext.getCurrentId(), permission);
            throw permissionDenied();
        }
    }

    /**
     * 构造「无权限」业务异常
     *
     * @return 403 业务异常
     */
    private AdminException permissionDenied() {
        return new AdminException(MessageConstant.ADMIN_PERMISSION_DENIED, ErrorCodeConstant.FORBIDDEN);
    }
}
