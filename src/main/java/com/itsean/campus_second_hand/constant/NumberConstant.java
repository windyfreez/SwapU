package com.itsean.campus_second_hand.constant;

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
    public static final String HOT_PRODUCTS_REDIS_KEY = "hot:products";
    // 分区随机排序相关常量
    public static final int ZONE_COUNT = 3;
    public static final int MAX_RANDOM_PICK_PER_ZONE = 5;

}
