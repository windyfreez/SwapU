package com.itsean.swapu_admin.service.impl;

import com.itsean.common.constant.NumberConstant;
import com.itsean.common.constant.StringConstant;
import com.itsean.swapu_admin.constant.ErrorCodeConstant;
import com.itsean.swapu_admin.dto.TrafficTrendQueryDTO;
import com.itsean.swapu_admin.exception.AdminException;
import com.itsean.swapu_admin.mapper.StatisticsMapper;
import com.itsean.swapu_admin.vo.CategorySalesVO;
import com.itsean.swapu_admin.vo.ProductStatusStatVO;
import com.itsean.swapu_admin.vo.StatisticsOverviewVO;
import com.itsean.swapu_admin.vo.TrafficTrendVO;
import com.itsean.swapu_admin.vo.TrendPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 平台统计服务测试：日期区间归一化、缺失日期补零、状态映射、Top N 夹取
 */
@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private StatisticsMapper statisticsMapper;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    private TrendPoint point(LocalDate date, String value) {
        TrendPoint point = new TrendPoint();
        point.setStatDate(date);
        point.setStatValue(new BigDecimal(value));
        return point;
    }

    private void stubEmptyTrends() {
        when(statisticsMapper.viewTrend(any(), any(), anyInt())).thenReturn(Collections.emptyList());
        when(statisticsMapper.orderTrend(any(), any())).thenReturn(Collections.emptyList());
        when(statisticsMapper.amountTrend(any(), any())).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("不传日期时默认最近 7 天，并补齐缺失日期为 0")
    void defaultRangeIsLast7Days() {
        stubEmptyTrends();

        List<TrafficTrendVO> result = statisticsService.trafficTrend(new TrafficTrendQueryDTO());

        assertEquals(7, result.size());
        assertEquals(LocalDate.now().toString(), result.get(6).getDate());
        assertEquals(0L, result.get(0).getViewCount());
        assertEquals(BigDecimal.ZERO, result.get(0).getAmount());
    }

    @Test
    @DisplayName("只有部分日期有数据时，其余日期补 0")
    void missingDaysFilledWithZero() {
        LocalDate start = LocalDate.of(2026, 11, 20);
        LocalDate end = LocalDate.of(2026, 11, 22);

        when(statisticsMapper.viewTrend(start, end, NumberConstant.BEHAVIOR_TYPE_VIEW))
                .thenReturn(List.of(point(LocalDate.of(2026, 11, 21), "5")));
        when(statisticsMapper.orderTrend(start, end)).thenReturn(Collections.emptyList());
        when(statisticsMapper.amountTrend(start, end))
                .thenReturn(List.of(point(LocalDate.of(2026, 11, 21), "120.50")));

        TrafficTrendQueryDTO query = new TrafficTrendQueryDTO();
        query.setStartDate(start);
        query.setEndDate(end);

        List<TrafficTrendVO> result = statisticsService.trafficTrend(query);

        assertEquals(3, result.size());
        assertEquals("2026-11-20", result.get(0).getDate());
        assertEquals(0L, result.get(0).getViewCount());
        assertEquals(5L, result.get(1).getViewCount());
        assertEquals(new BigDecimal("120.50"), result.get(1).getAmount());
        assertEquals(0L, result.get(2).getViewCount());
    }

    @Test
    @DisplayName("开始日期晚于结束日期时抛参数异常，且不查库")
    void reversedRangeRejected() {
        TrafficTrendQueryDTO query = new TrafficTrendQueryDTO();
        query.setStartDate(LocalDate.of(2026, 11, 22));
        query.setEndDate(LocalDate.of(2026, 11, 20));

        AdminException ex = assertThrows(AdminException.class, () -> statisticsService.trafficTrend(query));

        assertEquals(ErrorCodeConstant.STATISTICS_PARAM_ERROR, ex.getCode());
        verifyNoInteractions(statisticsMapper);
    }

    @Test
    @DisplayName("跨度超过 90 天时被拒绝")
    void tooLongRangeRejected() {
        TrafficTrendQueryDTO query = new TrafficTrendQueryDTO();
        query.setStartDate(LocalDate.of(2026, 1, 1));
        query.setEndDate(LocalDate.of(2026, 12, 31));

        AdminException ex = assertThrows(AdminException.class, () -> statisticsService.trafficTrend(query));

        assertEquals(ErrorCodeConstant.STATISTICS_PARAM_ERROR, ex.getCode());
        verifyNoInteractions(statisticsMapper);
    }

    @Test
    @DisplayName("商品状态分布过滤数量为 0 的状态，并翻译成中文描述")
    void productStatusFiltersZeroAndMapsDesc() {
        ProductStatusStatVO selling = new ProductStatusStatVO();
        selling.setStatus(NumberConstant.PRODUCT_STATUS_SELLING);
        selling.setCount(5);

        ProductStatusStatVO soldOut = new ProductStatusStatVO();
        soldOut.setStatus(NumberConstant.PRODUCT_STATUS_SOLD_OUT);
        soldOut.setCount(0);

        when(statisticsMapper.productStatusDistribution()).thenReturn(List.of(selling, soldOut));

        List<ProductStatusStatVO> result = statisticsService.productStatusDistribution();

        assertEquals(1, result.size());
        assertEquals(StringConstant.PRODUCT_STATUS_SELLING_DESC, result.get(0).getStatusDesc());
    }

    @Test
    @DisplayName("分类销售的 limit 缺省 10、上限 50")
    void categorySalesClampsLimit() {
        when(statisticsMapper.categorySales(anyInt())).thenReturn(new ArrayList<CategorySalesVO>());

        statisticsService.categorySales(null);
        verify(statisticsMapper).categorySales(10);

        statisticsService.categorySales(999);
        verify(statisticsMapper).categorySales(50);

        statisticsService.categorySales(3);
        verify(statisticsMapper).categorySales(3);
    }

    @Test
    @DisplayName("总览直接透传 Mapper 结果")
    void overviewDelegates() {
        StatisticsOverviewVO expected = new StatisticsOverviewVO();
        expected.setOnSaleCount(78);
        when(statisticsMapper.overview()).thenReturn(expected);

        assertSame(expected, statisticsService.overview());
    }

    @Test
    @DisplayName("空表时总览不会出现 null 值（Mapper 已用 ifnull 兜底）")
    void overviewWithZeroValues() {
        StatisticsOverviewVO expected = new StatisticsOverviewVO();
        expected.setOnSaleCount(0);
        expected.setPendingAuditCount(0);
        expected.setSoldCount(0);
        expected.setTotalAmount(BigDecimal.ZERO);
        expected.setTotalViewCount(0L);
        expected.setTotalUserCount(0);
        expected.setTodayOrderCount(0);
        expected.setTodayAmount(BigDecimal.ZERO);
        when(statisticsMapper.overview()).thenReturn(expected);

        StatisticsOverviewVO actual = statisticsService.overview();

        assertTrue(actual.getTotalAmount().compareTo(BigDecimal.ZERO) == 0);
        assertEquals(0L, actual.getTotalViewCount());
    }

    @Test
    @DisplayName("单日区间（start = end）也会返回 1 个点")
    void singleDayRange() {
        LocalDate day = LocalDate.of(2026, 11, 21);
        when(statisticsMapper.viewTrend(eq(day), eq(day), anyInt())).thenReturn(Collections.emptyList());
        when(statisticsMapper.orderTrend(eq(day), eq(day))).thenReturn(Collections.emptyList());
        when(statisticsMapper.amountTrend(eq(day), eq(day))).thenReturn(Collections.emptyList());

        TrafficTrendQueryDTO query = new TrafficTrendQueryDTO();
        query.setStartDate(day);
        query.setEndDate(day);

        List<TrafficTrendVO> result = statisticsService.trafficTrend(query);

        assertEquals(1, result.size());
        assertEquals("2026-11-21", result.get(0).getDate());
    }
}
