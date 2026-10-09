package com.itsean.campus_second_hand.constant;

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

    //Redis常量
    public static final String PRODUCT_VIEW_PREFIX = "product:view:count:";
    public static final String CATEGORY_LIST_PREFIX = "category:list:";
    public static final String HOT_PRODUCTS_REDIS_KEY = "hot:products:";
    public static final String USER_PROFILE_REDIS_KEY_PREFIX = "recommend:user:profile:";
    public static final String PRODUCT_STOCK_PREFIX = "product:stock:";

    //RabbitMQ常量
    public static final String EXCHANGE_STOCK = "swapu.stock.exchange";//库存交换机
    public static final String QUEUE_STOCK_SYNC = "swapu.stock.sync.queue";//库存同步队列
    public static final String EXCHANGE_STOCK_DLX = "swapu.stock.dlx.exchange";//库存死信交换机
    public static final String QUEUE_STOCK_DLQ = "swapu.stock.dlq.queue";//库存死信队列
    public static final String ROUTING_KEY_STOCK_CHANGE = "stock.change";//库存变更路由键
    public static final String ROUTING_KEY_STOCK_DEAD = "stock.change.dead";//库存死信路由键

    //库存变更流水号后缀
    public static final String STOCK_CHANGE_NO_DEDUCT_SUFFIX = "-D";//扣减
    public static final String STOCK_CHANGE_NO_RESTORE_SUFFIX = "-R";//回补

}
