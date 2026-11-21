package com.itsean.swapu_admin.constant;

/**
 * 管理员操作日志的动作类型
 * <p>
 * 只覆盖「管理员管理」相关的写操作；查询类操作不记录，避免日志量失控。
 */
public enum AdminLogAction {

    /** 新增管理员 */
    CREATE("新增管理员"),

    /** 修改管理员（姓名/头像/角色/重置密码） */
    UPDATE("修改管理员"),

    /** 启用/禁用管理员 */
    STATUS("启停管理员"),

    /** 删除管理员（逻辑删除） */
    DELETE("删除管理员"),

    /** 管理员修改自己的密码 */
    RESET_PASSWORD("修改密码");

    private final String description;

    AdminLogAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 按枚举名称取中文描述，用于日志列表展示
     *
     * @param action 枚举名称
     * @return 中文描述，未知值原样返回
     */
    public static String descriptionOf(String action) {
        if (action == null) {
            return null;
        }
        for (AdminLogAction value : values()) {
            if (value.name().equals(action)) {
                return value.description;
            }
        }
        return action;
    }
}
