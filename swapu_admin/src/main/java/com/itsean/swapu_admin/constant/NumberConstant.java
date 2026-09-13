package com.itsean.swapu_admin.constant;

/**
 *  数字常量类
 */
public class NumberConstant {
    //账户状态常量
    public static final int ACCOUNT_STATUS_NORMAL = 1;
    public static final int ACCOUNT_STATUS_LOCK = 0;
    //信用分常量
    public static final int FULL_CREDIT_SCORE = 100;
    //商品状态常量
    public static final int PRODUCT_STATUS_CHECKED = 0;//审核中
    public static final int PRODUCT_STATUS_SELLING = 1;//售卖中
    public static final int PRODUCT_STATUS_SOLD_OUT = 2;//已售出
    public static final int PRODUCT_STATUS_DOWN = 3;//已下架
    public static final Integer PRODUCT_STATUS_UP = 1;//已上架
    //商品浏览数常量
    public static final int DEFAULT_VIEW_COUNT = 0;//初始化商品浏览数
    // 热门商品相关常量
    public static final int HOT_PRODUCT_LIMIT = 20;
    // 分区随机排序相关常量
    public static final int ZONE_COUNT = 3;
    public static final int MAX_RANDOM_PICK_PER_ZONE = 5;

    //管理员角色常量
    public static final Integer ADMIN_ROLE_SUPER = 1;//超级管理员
    public static final Integer ADMIN_ROLE_NORMAL = 2;//普通管理员
    //管理员账号状态常量
    public static final Integer ADMIN_STATUS_ENABLED = 1;//启用
    public static final Integer ADMIN_STATUS_DISABLED = 0;//禁用
    //账号、密码长度限制
    public static final int ADMIN_USERNAME_MIN_LENGTH = 2;
    public static final int ADMIN_USERNAME_MAX_LENGTH = 20;
    public static final int ADMIN_PASSWORD_MIN_LENGTH = 6;
    public static final int ADMIN_PASSWORD_MAX_LENGTH = 20;

}
