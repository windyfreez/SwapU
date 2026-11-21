package com.itsean.swapu_admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 流量趋势查询条件
 * <p>
 * 两个参数都可以不传：缺省取最近 7 天；只传一个时另一端自动补齐。
 */
@Data
@ApiModel(description = "流量趋势查询条件")
public class TrafficTrendQueryDTO {

    @ApiModelProperty("开始日期 yyyy-MM-dd，缺省为结束日期前 6 天")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @ApiModelProperty("结束日期 yyyy-MM-dd，缺省为今天")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}
