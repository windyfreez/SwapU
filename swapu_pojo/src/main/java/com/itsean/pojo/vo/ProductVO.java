package com.itsean.pojo.vo;

import lombok.Data;

@Data
public class ProductVO {

    private Long productId;

    private Integer status;//状态：1.审核中 2.已上架
}
