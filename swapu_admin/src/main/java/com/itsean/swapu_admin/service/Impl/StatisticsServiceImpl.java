package com.itsean.swapu_admin.service.impl;

import com.itsean.common.constant.MessageConstant;
import com.itsean.common.constant.NumberConstant;
import com.itsean.common.constant.StringConstant;
import com.itsean.swapu_admin.constant.ErrorCodeConstant;
import com.itsean.swapu_admin.dto.TrafficTrendQueryDTO;
import com.itsean.swapu_admin.exception.AdminException;
import com.itsean.swapu_admin.mapper.StatisticsMapper;
import com.itsean.swapu_admin.service.StatisticsService;
import com.itsean.swapu_admin.vo.CategorySalesVO;
import com.itsean.swapu_admin.vo.ProductStatusStatVO;
import com.itsean.swapu_admin.vo.StatisticsOverviewVO;
import com.itsean.swapu_admin.vo.TrafficTrendVO;
import com.itsean.swapu_admin.vo.TrendPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 平台统计服务实现
 * <p>
 * 说明：Mapper 只返回"有数据的日期"，这里统一补齐缺失日期并转成前端好用的结构，
 * 否则折线图会出现断点。
 */
@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    /**
     * 缺省展示天数（含今天）
     */
    private static final int DEFAULT_DAYS = 7;

    /**
     * 最大可查询跨度（含首尾），避免前端传一个超大区间把库拖垮
     */
    private static final int MAX_SPAN_DAYS = 90;

    /**
     * 分类销售默认 Top N 与上限
     */
    private static final int DEFAULT_CATEGORY_TOP_N = 10;
    private static final int MAX_CATEGORY_TOP_N = 50;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private StatisticsMapper statisticsMapper;

    @Override
    public StatisticsOverviewVO overview() {
        return statisticsMapper.overview();
    }

    @Override
    public List<TrafficTrendVO> trafficTrend(TrafficTrendQueryDTO query) {
        LocalDate[] range = resolveRange(query);
        LocalDate start = range[0];
        LocalDate end = range[1];

        // 三个指标分别聚合，再在内存里按日期合并（不依赖 MySQL 8 的递归 CTE，兼容 5.7）
        Map<LocalDate, Long> views = toCountMap(statisticsMapper.viewTrend(
                start, end, NumberConstant.BEHAVIOR_TYPE_VIEW));
        Map<LocalDate, Long> orders = toCountMap(statisticsMapper.orderTrend(start, end));
        Map<LocalDate, BigDecimal> amounts = toAmountMap(statisticsMapper.amountTrend(start, end));

        List<TrafficTrendVO> result = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            TrafficTrendVO point = new TrafficTrendVO();
            point.setDate(day.format(DATE_FORMATTER));
            point.setViewCount(views.getOrDefault(day, 0L));
            point.setOrderCount(orders.getOrDefault(day, 0L));
            point.setAmount(amounts.getOrDefault(day, BigDecimal.ZERO));
            result.add(point);
        }
        return result;
    }

    @Override
    public List<ProductStatusStatVO> productStatusDistribution() {
        return statisticsMapper.productStatusDistribution().stream()
                // 数量为 0 的状态在饼图上只会产生噪音
                .filter(item -> item.getCount() != null && item.getCount() > 0)
                .peek(item -> item.setStatusDesc(productStatusDesc(item.getStatus())))
                .collect(Collectors.toList());
    }

    @Override
    public List<CategorySalesVO> categorySales(Integer limit) {
        int topN = limit == null ? DEFAULT_CATEGORY_TOP_N : limit;
        if (topN < 1) {
            topN = DEFAULT_CATEGORY_TOP_N;
        }
        if (topN > MAX_CATEGORY_TOP_N) {
            topN = MAX_CATEGORY_TOP_N;
        }
        return statisticsMapper.categorySales(topN);
    }

    /**
     * 归一化并校验日期区间
     *
     * @param query 查询条件，可为 null
     * @return [start, end]，闭区间
     */
    private LocalDate[] resolveRange(TrafficTrendQueryDTO query) {
        LocalDate end = query == null ? null : query.getEndDate();
        LocalDate start = query == null ? null : query.getStartDate();

        if (end == null) {
            end = LocalDate.now();
        }
        if (start == null) {
            start = end.minusDays(DEFAULT_DAYS - 1L);
        }

        if (start.isAfter(end)) {
            throw new AdminException(MessageConstant.STATISTICS_DATE_RANGE_ERROR,
                    ErrorCodeConstant.STATISTICS_PARAM_ERROR);
        }
        // 闭区间跨度：start=end 时为 1 天
        long span = ChronoUnit.DAYS.between(start, end) + 1;
        if (span > MAX_SPAN_DAYS) {
            log.warn("统计区间过大被拒绝，start：{}，end：{}，span：{}", start, end, span);
            throw new AdminException(MessageConstant.STATISTICS_DATE_RANGE_ERROR,
                    ErrorCodeConstant.STATISTICS_PARAM_ERROR);
        }
        return new LocalDate[]{start, end};
    }

    /**
     * 计数型趋势转 Map
     */
    private Map<LocalDate, Long> toCountMap(List<TrendPoint> points) {
        Map<LocalDate, Long> map = new HashMap<>();
        for (TrendPoint point : points) {
            if (point.getStatDate() == null) {
                continue;
            }
            BigDecimal value = point.getStatValue() == null ? BigDecimal.ZERO : point.getStatValue();
            map.put(point.getStatDate(), value.longValue());
        }
        return map;
    }

    /**
     * 金额型趋势转 Map
     */
    private Map<LocalDate, BigDecimal> toAmountMap(List<TrendPoint> points) {
        Map<LocalDate, BigDecimal> map = new HashMap<>();
        for (TrendPoint point : points) {
            if (point.getStatDate() == null) {
                continue;
            }
            map.put(point.getStatDate(), point.getStatValue() == null ? BigDecimal.ZERO : point.getStatValue());
        }
        return map;
    }

    /**
     * 商品状态码转中文描述
     *
     * @param status 状态值
     * @return 中文描述
     */
    private String productStatusDesc(Integer status) {
        if (status == null) {
            return "未知状态";
        }
        switch (status) {
            case NumberConstant.PRODUCT_STATUS_CHECKED:
                return StringConstant.PRODUCT_STATUS_CHECKED_DESC;
            case NumberConstant.PRODUCT_STATUS_SELLING:
                return StringConstant.PRODUCT_STATUS_SELLING_DESC;
            case NumberConstant.PRODUCT_STATUS_SOLD_OUT:
                return StringConstant.PRODUCT_STATUS_SOLD_OUT_DESC;
            case NumberConstant.PRODUCT_STATUS_DOWN:
                return StringConstant.PRODUCT_STATUS_DOWN_DESC;
            case NumberConstant.PRODUCT_STATUS_AUDIT_REJECTED:
                return StringConstant.PRODUCT_STATUS_AUDIT_REJECTED_DESC;
            default:
                return "未知状态";
        }
    }
}
