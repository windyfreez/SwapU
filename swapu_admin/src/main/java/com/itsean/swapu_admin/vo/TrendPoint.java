package com.itsean.swapu_admin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 趋势聚合的中间行（Mapper 专用）
 * <p>
 * 浏览量/订单数是计数，成交额是金额，统一用 BigDecimal 承接，
 * 由 Service 转成 {@link TrafficTrendVO} 并补齐缺失日期。
 */
@Data
public class TrendPoint {

    private LocalDate statDate;

    private BigDecimal statValue;
}
