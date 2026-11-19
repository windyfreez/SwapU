package com.itsean.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 取消订单请求参数
 */
@Data
public class OrderCancelDTO {

    /** 订单编号，必填 */
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    /** 取消原因，对应 cancel_reason varchar(500) */
    @Size(max = 500, message = "取消原因不能超过500个字符")
    private String cancelReason;
}
