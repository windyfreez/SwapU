package com.itsean.campus_second_hand.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 库存变更日志实体：MQ 异步落库的幂等、审计与对账依据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockChangeLog {

    private Long id;

    private String changeNo;//库存变更流水号（幂等键）

    private String orderNo;//业务单号

    private Long productId;//商品id

    private Integer quantity;//变更数量

    private Integer operationType;//1=扣减 2=回补

    private Integer status;//0=待处理 1=成功 2=失败

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;//创建时间

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;//更新时间

}
