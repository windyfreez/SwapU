package com.itsean.campus_second_hand.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;

    private String title;

    private String description;

    private Integer categoryId;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer productCondition;//成色：1.全新（未拆封） 2.几乎全新（已经拆封） 3.有使用痕迹 4.明显使用痕迹

    private List<String> images;

    private Integer quantity = 1;

}
