package com.itsean.swapu_admin.constant;

/**
 *  数字常量类
 */
public class NumberConstant {
    //账户状态常量
    public static final int ACCOUNT_STATUS_NORMAL = 1;
    public static final int ACCOUNT_STATUS_LOCK = 0;
    //信用分常量
    public static final int FULL_CREDIT_SCORE = 100;//满分
    public static final int CREDIT_SCORE_MIN = 0;//最低信用分
    //商品状态常量
    public static final int PRODUCT_STATUS_CHECKED = 0;//审核中
    public static final int PRODUCT_STATUS_SELLING = 1;//售卖中
    public static final int PRODUCT_STATUS_SOLD_OUT = 2;//已售出
    public static final int PRODUCT_STATUS_DOWN = 3;//已下架
    public static final int PRODUCT_STATUS_AUDIT_REJECTED = 4;//审核驳回
    public static final Integer PRODUCT_STATUS_UP = 1;//已上架
    //评论审核状态常量
    public static final int COMMENT_STATUS_PENDING = 0;//待审核
    public static final int COMMENT_STATUS_PUBLISHED = 1;//已发布
    public static final int COMMENT_STATUS_REJECTED = 2;//审核驳回
    //审核类型常量
    public static final int AUDIT_TYPE_PRODUCT = 1;//商品
    public static final int AUDIT_TYPE_COMMENT = 2;//评论
    public static final int AUDIT_TYPE_AVATAR = 3;//头像
    //审核结论常量
    public static final int AUDIT_RESULT_PASS = 1;//通过
    public static final int AUDIT_RESULT_REJECT = 2;//驳回
    //驳回原因长度上限，与 audit_record.reason 的 varchar(500) 保持一致
    public static final int AUDIT_REASON_MAX_LENGTH = 500;
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

    //分类状态常量
    public static final Integer CATEGORY_STATUS_ENABLED = 1;//启用
    public static final Integer CATEGORY_STATUS_DISABLED = 0;//禁用
    //分类默认排序值
    public static final int DEFAULT_CATEGORY_SORT = 0;
    //分类名称长度上限，与 category.name 的 varchar(50) 保持一致
    public static final int CATEGORY_NAME_MAX_LENGTH = 50;

}
