package com.itsean.swapu_admin.dto;

import lombok.Data;

@Data
public class OrderDeliverDTO {
    private String orderNo;

    private String logisticsCompany;

    private String logisticsNo;
}
