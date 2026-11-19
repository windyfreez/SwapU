package com.itsean.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 订单发货请求参数
 */
@Data
public class OrderDeliverDTO {

    /** 订单编号，必填 */
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    /** 物流公司/跑腿平台，对应 logistics_company varchar(100) */
    @Size(max = 100, message = "物流公司不能超过100个字符")
    private String logisticsCompany;

    /** 物流单号/跑腿单号，对应 logistics_no varchar(100) */
    @Size(max = 100, message = "物流单号不能超过100个字符")
    private String logisticsNo;
}
