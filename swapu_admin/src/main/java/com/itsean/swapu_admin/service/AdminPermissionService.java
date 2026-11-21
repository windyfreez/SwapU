package com.itsean.swapu_admin.service;

import com.itsean.swapu_admin.constant.AdminPermission;

import java.util.Set;

/**
 * 管理端权限服务：负责「当前登录管理员拥有哪些权限」的查询与校验
 * <p>
 * 角色信息不写入 JWT，而是每次按 adminId 查库重新计算，
 * 这样超管把某个管理员降级或禁用后能立即生效（代价是每个请求一次查询，管理端流量很小）。
 */
public interface AdminPermissionService {

    /**
     * 查询当前登录管理员的权限集合
     *
     * @return 权限集合，不会为空集合以外的异常值
     * @throws com.itsean.swapu_admin.exception.AdminException 未登录、账号不存在或已禁用
     */
    Set<AdminPermission> currentPermissions();

    /**
     * 判断当前登录管理员是否拥有指定权限
     *
     * @param permission 权限点
     * @return true 拥有
     */
    boolean hasPermission(AdminPermission permission);

    /**
     * 校验当前登录管理员是否拥有指定权限，不拥有则抛出业务异常（错误码 403）
     *
     * @param permission 权限点
     */
    void check(AdminPermission permission);
}
