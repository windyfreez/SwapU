package com.itsean.common.constant;

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

    //4.商品类信息提示
    public static final String CANT_SEARCH_HOT_PRODUCTS = "未查询到热门商品数据";

    //5.商品类型信息提示
    public static final String CATEGORY_NOT_EXIST = "商品类型不存在";

    //6.评论类信息提示
    public static final String COMMENT_NOT_EXIST = "评论不存在";
    public static final String CANT_DELETE_OTHERS_COMMENT = "只能删除自己发出的评论";
    public static final String CANT_COMMENT_YOURSELF = "不能给自己评论";
    public static final String COMMENT_CONTENT_EMPTY = "评论内容不能为空";

    public static final String PRODUCT_NOT_EXIST = "商品不存在或已下架";
    public static final String ORDER_NOT_EXIST = "订单不存在";
    public static final String ORDER_STATUS_CANT_REFUND = "该订单当前状态不支持退货退款，只有待发货的订单可以申请";
    public static final String ORDER_CANT_APPROVE_REFUND = "该订单当前没有待审核的退货申请";
    public static final String ONLY_BUYER_CAN_APPLY_REFUND = "只有买家本人可以申请退货退款";
    public static final String ONLY_SELLER_CAN_APPROVE_REFUND = "只有卖家本人可以审核退货申请";
    public static final String ORDER_STATUS_CANT_CANCEL_APPLY = "只有待接单或待支付的订单可以取消";
    public static final String ONLY_BUYER_CAN_CANCEL = "只有买家本人可以取消订单";
    public static final String ONLY_SELLER_CAN_AUDIT_CANCEL = "只有卖家本人可以审核取消申请";
    public static final String ORDER_CANT_APPROVE_CANCEL = "该订单当前没有待审核的取消申请";
    public static final String AI_PROMPT_EMPTY = "提问内容不能为空";
    public static final String AI_PROMPT_TOO_LONG = "提问内容过长，请精简后再试";
    public static final String AI_NOT_CONFIGURED = "AI 客服暂未开通，请联系管理员";
    public static final String AI_BALANCE_NOT_ENOUGH = "AI 客服暂不可用，请联系管理员";
    public static final String AI_REQUEST_TOO_FREQUENT = "提问太快啦，请稍等片刻再问";
    public static final String AI_SERVICE_UNAVAILABLE = "AI 客服暂时联系不上，请稍后再试";
    public static final String AI_EMPTY_ANSWER = "AI 客服没有给出回答，换个说法再问一次试试";
    public static final String ADMIN_USERNAME_OR_PASSWORD_ERROR = "账号或密码错误";
    public static final String ADMIN_ACCOUNT_DISABLED = "账号已被禁用";
    public static final String ADMIN_USERNAME_ALREADY_EXIST = "用户名已存在";
    public static final String ADMIN_NOT_EXIST = "管理员不存在";
    public static final String ADMIN_NO_PERMISSION = "无操作权限，仅超级管理员可操作";
    public static final String ADMIN_CANNOT_DISABLE_SELF = "不能禁用自己";
    public static final String ADMIN_CANNOT_DELETE_SELF = "不能删除自己";
    public static final String ADMIN_MUST_KEEP_ONE_SUPER = "至少保留一名启用的超级管理员";
    public static final String ADMIN_OLD_PASSWORD_ERROR = "输入的旧密码不正确";
    public static final String ADMIN_OLD_NEW_PASSWORD_SAME = "新密码和旧密码相同";
    public static final String ADMIN_USERNAME_LENGTH_ERROR = "账号长度需为2-20个字符";
    public static final String ADMIN_PASSWORD_LENGTH_ERROR = "密码长度需为6-20位";
    public static final String ADMIN_STATUS_ERROR = "管理员状态值不合法";
    /** 当前登录管理员所属角色不具备该操作所需权限 */
    public static final String ADMIN_PERMISSION_DENIED = "当前角色无权执行该操作";
    /** 角色取值不合法（只允许 1/2/3） */
    public static final String ADMIN_ROLE_ERROR = "管理员角色取值不合法";
    /** 不允许修改自己的角色 */
    public static final String ADMIN_CANNOT_CHANGE_SELF_ROLE = "不能修改自己的角色，请让其他超级管理员操作";
    /** 平台统计的日期区间不合法 */
    public static final String STATISTICS_DATE_RANGE_ERROR = "统计区间不合法：开始日期不能晚于结束日期，且跨度不能超过90天";
    public static final String CATEGORY_NAME_ALREADY_EXIST = "分类名称已存在";
    public static final String CATEGORY_NOT_ALLOW_DISABLE = "该分类下存在在售商品，请先下架商品再禁用分类";
    public static final String CATEGORY_HAS_PRODUCT = "该分类下存在商品，无法删除";
    public static final String CATEGORY_NAME_REQUIRED = "分类名称不能为空";
    public static final String CATEGORY_NAME_LENGTH_ERROR = "分类名称长度不能超过50个字符";
    public static final String CATEGORY_STATUS_ERROR = "分类状态值不合法";
    public static final String USER_STATUS_ERROR = "用户状态值不合法";
    public static final String USER_CREDIT_SCORE_ERROR = "信用分需在0到100之间";
    public static final String AUDIT_PRODUCT_NOT_EXIST = "商品不存在";
    public static final String AUDIT_COMMENT_NOT_EXIST = "评论不存在";
    public static final String AUDIT_PRODUCT_ID_REQUIRED = "商品ID不能为空";
    public static final String AUDIT_COMMENT_ID_REQUIRED = "评论ID不能为空";
    public static final String AUDIT_REASON_REQUIRED = "驳回时必须填写原因";
    public static final String AUDIT_REASON_TOO_LONG = "驳回原因不能超过500个字符";
    public static final String AUDIT_PRODUCT_STATUS_ERROR = "商品当前状态不可审核";
    public static final String AUDIT_COMMENT_STATUS_ERROR = "评论当前状态不可审核";
    public static final String AUDIT_RESULT_ERROR = "审核结论不合法";
    public static final String AUDIT_STATUS_FILTER_ERROR = "审核状态筛选值不合法";
    public static final String AUDIT_TYPE_FILTER_ERROR = "审核类型筛选值不合法";

    // ===== 订单权限校验（P0 安全修复）=====
    /** 非买家/卖家本人查询订单 */
    public static final String CANT_VIEW_OTHERS_ORDER = "无权查看该订单";
    /** 只有卖家可以确认接单 */
    public static final String ONLY_SELLER_CAN_CONFIRM = "只有卖家本人可以确认接单";
    /** 只有买家可以支付 */
    public static final String ONLY_BUYER_CAN_PAY = "只有买家本人可以支付该订单";
    /** 只有卖家可以发货 */
    public static final String ONLY_SELLER_CAN_DELIVER = "只有卖家本人可以发货";
    /** 只有买家可以确认收货 */
    public static final String ONLY_BUYER_CAN_RECEIVE = "只有买家本人可以确认收货";
    /** 订单状态不允许接单 */
    public static final String ORDER_STATUS_CANT_CONFIRM = "该订单当前状态不支持卖家接单";
    /** 购买数量非法（负数会导致反向加库存、订单金额为负） */
    public static final String QUANTITY_INVALID = "购买数量必须大于0";
    /** 余额支付未输入支付密码 */
    public static final String PAY_PASSWORD_REQUIRED = "余额支付需要输入支付密码";

    // ===== 以下合并自 g-swapu_admin-MessageConstant.java =====

}
