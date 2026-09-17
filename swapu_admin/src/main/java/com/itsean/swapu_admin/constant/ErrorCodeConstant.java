package com.itsean.swapu_admin.constant;

/**
 * 管理端业务错误码常量类
 * <p>
 * 约定：401 未登录/无操作权限；4101-4105 管理员模块；4201-4203 审核模块；4301-4303 分类模块；4401-4402 用户模块。
 */
public class ErrorCodeConstant {

    /** 未登录 / token 过期 / 无操作权限 */
    public static final Integer UNAUTHORIZED = 401;

    //管理员模块错误码 4101-4105
    public static final Integer ADMIN_USERNAME_OR_PASSWORD_ERROR = 4101;//账号或密码错误
    public static final Integer ADMIN_ACCOUNT_DISABLED = 4102;//账号已被禁用
    public static final Integer ADMIN_USERNAME_ALREADY_EXIST = 4103;//用户名已存在
    public static final Integer ADMIN_CANNOT_OPERATE_SELF = 4104;//不能禁用或删除自己
    public static final Integer ADMIN_MUST_KEEP_ONE_SUPER = 4105;//至少保留一名启用的超级管理员

    //分类模块错误码 4301-4303
    public static final Integer CATEGORY_NAME_ALREADY_EXIST = 4301;//分类名称已存在
    public static final Integer CATEGORY_HAS_PRODUCT = 4302;//分类下存在商品，不可禁用或删除
    public static final Integer CATEGORY_NOT_EXIST = 4303;//分类不存在

}
