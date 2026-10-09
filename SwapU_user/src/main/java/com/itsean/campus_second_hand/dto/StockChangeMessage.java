package com.itsean.campus_second_hand.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 库存变更消息体：发送到 RabbitMQ 的扣减/回补消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockChangeMessage {

    private String changeNo;//库存变更流水号（幂等键）

    private String orderNo;//业务单号

    private Long productId;//商品id

    private Integer quantity;//变更数量

    private Integer operationType;//1=扣减 2=回补

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;//消息产生时间

}
