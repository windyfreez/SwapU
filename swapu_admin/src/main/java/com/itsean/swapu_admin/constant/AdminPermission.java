package com.itsean.swapu_admin.constant;

import com.itsean.common.constant.NumberConstant;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 管理端权限点，以及「角色 → 权限集合」的唯一映射
 * <p>
 * 设计说明：
 * 1) 接口上只声明需要哪个权限点（{@code @RequirePermission}），不直接判断角色值，
 *    这样后续调整某个角色的能力范围时，只改这里的映射表，不用改 Controller；
 * 2) 角色固定三种，不支持自定义角色；
 * 3) 角色值 3 用于运营管理员，是为了不改变已有数据（现存账号 1/2 语义不变）。
 */
public enum AdminPermission {

    /** 审核：商品审核、评论审核、审核流水查询 */
    AUDIT("审核"),

    /** 分类管理：新增、修改、启停、删除 */
    CATEGORY_MANAGE("分类管理"),

    /** 用户管理：列表、详情、启停、信用分、交易统计 */
    USER_MANAGE("用户管理"),

    /** 管理员管理：增删改启停、重置密码、查看管理员列表 */
    ADMIN_MANAGE("管理员管理");

    private final String description;

    AdminPermission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 角色与权限的映射表（不可变，静态初始化一次）
     */
    private static final Map<Integer, Set<AdminPermission>> ROLE_PERMISSIONS;

    static {
        Map<Integer, Set<AdminPermission>> map = new HashMap<>(4);
        // 超级管理员：全部权限
        map.put(NumberConstant.ADMIN_ROLE_SUPER,
                Collections.unmodifiableSet(EnumSet.allOf(AdminPermission.class)));
        // 运营管理员：审核 + 分类管理 + 用户管理（不含管理员管理）
        map.put(NumberConstant.ADMIN_ROLE_OPERATION,
                Collections.unmodifiableSet(EnumSet.of(AUDIT, CATEGORY_MANAGE, USER_MANAGE)));
        // 普通管理员：只有审核
        map.put(NumberConstant.ADMIN_ROLE_NORMAL,
                Collections.unmodifiableSet(EnumSet.of(AUDIT)));
        ROLE_PERMISSIONS = Collections.unmodifiableMap(map);
    }

    /**
     * 根据角色取权限集合，未知角色返回空集合（宁可少给权限，不能多给）
     *
     * @param role 角色值：1超级管理员 2普通管理员 3运营管理员
     * @return 权限集合，不可变
     */
    public static Set<AdminPermission> ofRole(Integer role) {
        if (role == null) {
            return Collections.emptySet();
        }
        return ROLE_PERMISSIONS.getOrDefault(role, Collections.emptySet());
    }

    /**
     * 判断角色值是否是系统支持的角色，用于新增/修改管理员时的取值校验
     *
     * @param role 角色值
     * @return true 是合法角色
     */
    public static boolean isKnownRole(Integer role) {
        return role != null && ROLE_PERMISSIONS.containsKey(role);
    }
}
