package com.itsean.campus_second_hand.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {

    //1.用户类信息提示
    public static final String PHONE_ALREADY_EXIST = "该手机号已存在";
    public static final String EMAIL_ALREADY_EXIST = "该邮箱已存在";
    public static final String STUDENT_ID_ALREADY_EXIST = "该学号已存在";
    public static final String PASSWORD_ERROR = "密码错误，请重试";
    public static final String USER_NOT_EXIST = "账户不存在";
    public static final String USER_LOCK = "账户已被锁定";
    public static final String OLD_NEW_PASSWORD_SAME = "新密码和旧密码相同";
    public static final String CONFIRM_NEW_PASSWORD_NOT_SAME = "确认密码和新密码不相同";
    public static final String OLD_PASSWORD_ERROR = "输入的旧密码不正确";
    public static final String UPLOAD_FAILED = "文件上传失败";

    //2.订单类信息提示
    public static final String NEED_MORE_THAN_STORE = "商品库存不足！";
    public static final String CANT_BUY_YOURSELF_PRODUCT = "您不能购买您自己的商品";
    public static final String ORDER_STATUS_CANT_CANCEL = "您的订单无法取消，请选择退款";
    public static final String BALANCE_NOT_ENOUGH = "您的余额不足，请充值或选择其他支付方式";
    public static final String ORDER_STATUS_CANT_PAY = "该订单目前还不能支付";
    public static final String ORDER_STATUS_CANT_DELIVER = "该订单目前还不能发货";
    public static final String ORDER_STATUS_CANT_RECEIVE = "该订单目前还不能收货";

    //3.聊天类信息提示
    public static final String CANT_SEND_MESSAGE_TO_YOURSELF = "不能给自己发送消息";

    //4.商品类信息提示
    public static final String CANT_SEARCH_HOT_PRODUCTS = "未查询到热门商品数据";
    public static final String PRODUCT_NOT_EXIST = "商品不存在或已下架";

    //5.商品类型信息提示
    public static final String CATEGORY_NOT_EXIST = "商品类型不存在";

    //6.退货退款类信息提示
    public static final String ORDER_NOT_EXIST = "订单不存在";
    public static final String ORDER_STATUS_CANT_REFUND = "该订单当前状态不支持退货退款，只有待发货的订单可以申请";
    public static final String ORDER_CANT_APPROVE_REFUND = "该订单当前没有待审核的退货申请";
    public static final String ONLY_BUYER_CAN_APPLY_REFUND = "只有买家本人可以申请退货退款";
    public static final String ONLY_SELLER_CAN_APPROVE_REFUND = "只有卖家本人可以审核退货申请";

    //7.取消订单审核类信息提示
    public static final String ORDER_STATUS_CANT_CANCEL_APPLY = "只有待接单或待支付的订单可以取消";
    public static final String ONLY_BUYER_CAN_CANCEL = "只有买家本人可以取消订单";
    public static final String ONLY_SELLER_CAN_AUDIT_CANCEL = "只有卖家本人可以审核取消申请";
    public static final String ORDER_CANT_APPROVE_CANCEL = "该订单当前没有待审核的取消申请";

    //8.AI客服类信息提示
    public static final String AI_PROMPT_EMPTY = "提问内容不能为空";
    public static final String AI_PROMPT_TOO_LONG = "提问内容过长，请精简后再试";
    public static final String AI_NOT_CONFIGURED = "AI 客服暂未开通，请联系管理员";
    public static final String AI_BALANCE_NOT_ENOUGH = "AI 客服暂不可用，请联系管理员";
    public static final String AI_REQUEST_TOO_FREQUENT = "提问太快啦，请稍等片刻再问";
    public static final String AI_SERVICE_UNAVAILABLE = "AI 客服暂时联系不上，请稍后再试";
    public static final String AI_EMPTY_ANSWER = "AI 客服没有给出回答，换个说法再问一次试试";
}
