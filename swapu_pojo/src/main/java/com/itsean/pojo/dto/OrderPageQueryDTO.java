package com.itsean.pojo.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import java.time.LocalDateTime;

/**
 * 订单分页查询参数
 */
@Data
public class OrderPageQueryDTO {
    public static final String I_AM_BUYER = "buyer";
    public static final String I_AM_SELLER = "seller";

    /** 页码，缺省由 service 补默认值 */
    @Min(value = 1, message = "页码不能小于1")
    private Integer page;

    /** 每页条数，上限 100，避免一次拉全表 */
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer pageSize;

    private Integer status;

    /** 视角：buyer 我是买家 / seller 我是卖家 */
    private String role;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long currentId;

}
