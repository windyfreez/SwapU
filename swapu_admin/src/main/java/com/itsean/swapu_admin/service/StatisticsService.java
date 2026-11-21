package com.itsean.swapu_admin.service;

import com.itsean.swapu_admin.dto.TrafficTrendQueryDTO;
import com.itsean.swapu_admin.vo.CategorySalesVO;
import com.itsean.swapu_admin.vo.ProductStatusStatVO;
import com.itsean.swapu_admin.vo.StatisticsOverviewVO;
import com.itsean.swapu_admin.vo.TrafficTrendVO;

import java.util.List;

/**
 * 平台统计服务（工作台数据源）
 */
public interface StatisticsService {

    /**
     * 平台总览指标
     *
     * @return 总览统计
     */
    StatisticsOverviewVO overview();

    /**
     * 流量趋势（按天，缺失日期补 0）
     *
     * @param query 日期区间，缺省最近 7 天
     * @return 按日期升序的数据点
     */
    List<TrafficTrendVO> trafficTrend(TrafficTrendQueryDTO query);

    /**
     * 商品状态分布
     *
     * @return 按状态升序、只含数量大于 0 的状态
     */
    List<ProductStatusStatVO> productStatusDistribution();

    /**
     * 分类成交额 Top N
     *
     * @param limit 取前 N，缺省 10、上限 50
     * @return 按成交额倒序
     */
    List<CategorySalesVO> categorySales(Integer limit);
}
