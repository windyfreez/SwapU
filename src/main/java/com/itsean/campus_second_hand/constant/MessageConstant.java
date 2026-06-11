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
    public static final String NEED_MORE_THAN_STORE = "您需要的商品数量大于卖家提供的最多商品数量";
    public static final String CANT_BUY_YOURSELF_PRODUCT = "您不能购买您自己的商品";
    public static final String ORDER_STATUS_CANT_CANCEL = "您的订单无法取消，请选择退款";
    public static final String BALANCE_NOT_ENOUGH = "您的余额不足，请充值或选择其他支付方式";
    public static final String ORDER_STATUS_CANT_PAY = "该订单目前还不能支付";
    public static final String ORDER_STATUS_CANT_DELIVER = "该订单目前还不能发货";
    public static final String ORDER_STATUS_CANT_RECEIVE = "该订单目前还不能收货";

    //3.聊天类信息提示
    public static final String CANT_SEND_MESSAGE_TO_YOURSELF = "不能给自己发送消息";
}
