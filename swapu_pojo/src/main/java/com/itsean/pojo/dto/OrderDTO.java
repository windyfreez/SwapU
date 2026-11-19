package com.itsean.pojo.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 创建订单请求参数
 */
@Data
public class OrderDTO {

    /** 商品ID，必填 */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 收货地址ID，面交时可空 */
    private Long addressId;

    /**
     * 配送方式：1快递 2外卖跑腿 3面交自提。
     * 允许不传：当前用户端下单页未收集该字段，由 service 层默认按「快递」处理
     */
    @Min(value = 1, message = "配送方式取值不合法")
    @Max(value = 3, message = "配送方式取值不合法")
    private Integer deliveryMethod;

    /**
     * 购买数量，必须为正数。
     * 负数会导致库存反向增加、订单金额为负，必须在入口处拦住
     */
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量必须大于0")
    @Max(value = 999, message = "单次购买数量不能超过999")
    private Integer quantity;

    /** 买家留言，对应 buyer_message varchar(500) */
    @Size(max = 500, message = "买家留言不能超过500个字符")
    private String buyerMessage;
}
