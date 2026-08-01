package com.itsean.pojo.dto;

import lombok.Data;

@Data
public class OrderDeliverDTO {
    private String orderNo;

    private String logisticsCompany;

    private String logisticsNo;
}
