package com.itsean.common.constant;

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
    //评论审核状态常量
    public static final int COMMENT_STATUS_CHECKING = 0;//审核中
    public static final int COMMENT_STATUS_APPROVED = 1;//过审
    public static final int COMMENT_STATUS_VIOLATION = 2;//违禁
    //评论类型常量
    public static final int COMMENT_TYPE_BAD = 0;//差评
    public static final int COMMENT_TYPE_GOOD = 1;//好评


    public static final int DEFAULT_PAGE = 1;//默认页码
    public static final int DEFAULT_PAGE_SIZE = 10;//默认每页记录数
    public static final int STOCK_CHANGE_STATUS_PENDING = 0;//待处理
    public static final int STOCK_CHANGE_STATUS_SUCCESS = 1;//成功
    public static final int STOCK_CHANGE_STATUS_FAILED = 2;//失败(死信/需人工)
    public static final int STOCK_CHANGE_OP_DEDUCT = 1;//扣减
    public static final int STOCK_CHANGE_OP_RESTORE = 2;//回补
    public static final int CREDIT_SCORE_MIN = 0;//最低信用分
    public static final int PRODUCT_STATUS_AUDIT_REJECTED = 4;//审核驳回
    public static final int COMMENT_STATUS_PENDING = 0;//待审核
    public static final int COMMENT_STATUS_PUBLISHED = 1;//已发布
    public static final int COMMENT_STATUS_REJECTED = 2;//审核驳回
    public static final int AUDIT_TYPE_PRODUCT = 1;//商品
    public static final int AUDIT_TYPE_COMMENT = 2;//评论
    public static final int AUDIT_TYPE_AVATAR = 3;//头像
    public static final int AUDIT_RESULT_PASS = 1;//通过
    public static final int AUDIT_RESULT_REJECT = 2;//驳回
    public static final int AUDIT_REASON_MAX_LENGTH = 500;
    public static final Integer ADMIN_ROLE_SUPER = 1;//超级管理员
    public static final Integer ADMIN_ROLE_NORMAL = 2;//普通管理员
    public static final Integer ADMIN_ROLE_OPERATION = 3;//运营管理员（审核 + 分类管理 + 用户管理）
    public static final Integer ADMIN_STATUS_ENABLED = 1;//启用
    public static final Integer ADMIN_STATUS_DISABLED = 0;//禁用
    public static final int ADMIN_USERNAME_MIN_LENGTH = 2;
    public static final int ADMIN_USERNAME_MAX_LENGTH = 20;
    public static final int ADMIN_PASSWORD_MIN_LENGTH = 6;
    public static final int ADMIN_PASSWORD_MAX_LENGTH = 20;
    public static final Integer CATEGORY_STATUS_ENABLED = 1;//启用
    public static final Integer CATEGORY_STATUS_DISABLED = 0;//禁用
    public static final int DEFAULT_CATEGORY_SORT = 0;
    public static final int CATEGORY_NAME_MAX_LENGTH = 50;

    // ===== 以下合并自 g-swapu_admin-NumberConstant.java =====

}
