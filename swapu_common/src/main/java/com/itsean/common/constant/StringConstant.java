package com.itsean.common.constant;

/**
 * 字符串常量类
 */
public class StringConstant {

    //商品状态
    public static final String PRODUCT_STATUS_CHECKED_DESC = "审核中";//审核中
    public static final String PRODUCT_STATUS_SELLING_DESC = "售卖中";//售卖中
    public static final String PRODUCT_STATUS_SOLD_OUT_DESC = "已售出";//已售出
    public static final String PRODUCT_STATUS_DOWN_DESC = "已下架";//已下架

    //商品成色
    public static final String PRODUCT_CONDITION_NEW = "全新（未拆封）";
    public static final String PRODUCT_CONDITION_ALMOST_NEW = "几乎全新（已经拆封）";
    public static final String PRODUCT_CONDITION_HAS_USED = "有使用痕迹";
    public static final String PRODUCT_CONDITION_STRONG_USED = "明显使用痕迹";
    public static final String PRODUCT_CONDITION_NOT_EXIST = "未知成色";

    //评论审核状态
    public static final String COMMENT_STATUS_CHECKING_DESC = "审核中";//审核中
    public static final String COMMENT_STATUS_APPROVED_DESC = "过审";//过审
    public static final String COMMENT_STATUS_VIOLATION_DESC = "违禁";//违禁

    //评论类型
    public static final String COMMENT_TYPE_BAD_DESC = "差评";//差评
    public static final String COMMENT_TYPE_GOOD_DESC = "好评";//好评

    //Redis常量
    public static final String PRODUCT_VIEW_PREFIX = "product:view:count:";
    public static final String CATEGORY_LIST_PREFIX = "category:list:";
    public static final String HOT_PRODUCTS_REDIS_KEY = "hot:products:";
    public static final String BEHAVIOR_QUEUE_KEY = "log:behavior:queue";




}
