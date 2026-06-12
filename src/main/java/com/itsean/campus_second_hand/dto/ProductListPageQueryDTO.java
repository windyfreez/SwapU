package com.itsean.campus_second_hand.dto;

import io.swagger.models.auth.In;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductListPageQueryDTO {

    private Integer page;

    private Integer pageSize;

    private Integer categoryId;

    private String keyword;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer productCondition;

    private Integer status;

    private String sort;

    private Long userId;

    private String title;

}
