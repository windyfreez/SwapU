package com.itsean.campus_second_hand.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDetailVO {

    private Long id;

    private String title;

    private String description;

    private Integer categoryId;

    private String categoryName;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer productCondition;

    private String productConditionDesc;

    private List<String> images;

    private Integer viewCount;

    private Integer favoriteCount;

    private Integer status;

    private String statusDesc;//todo 后续需要完善订单信息

    private SellerInfo sellerInfo;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    private Boolean isFavorite;//todo 后续需要完善收藏信息

    private String contactWay;

    private Integer quantity;

    @Data
    public static class SellerInfo {
        private Long id;

        private String username;

        private String avatar;

        private Integer creditScore;

        //todo 后续需要完善订单信息
        private Integer publishCount;

        private Integer soldCount;


    }
}
