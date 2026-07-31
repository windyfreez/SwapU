package com.itsean.campus_second_hand.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;

    private String orderNo;

    private Long productId;

    private String productTitle;

    private String productImage;

    private Integer quantity;

    private BigDecimal totalAmount;

    private Integer status;

    private String statusDesc;

    private String tradeUserName;

    private String tradeUserAvatar;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;

}
