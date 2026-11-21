package com.itsean.swapu_admin.mapper;

import com.itsean.swapu_admin.vo.CategorySalesVO;
import com.itsean.swapu_admin.vo.ProductStatusStatVO;
import com.itsean.swapu_admin.vo.StatisticsOverviewVO;
import com.itsean.swapu_admin.vo.TrendPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 平台统计 Mapper
 * <p>
 * 全部是只读聚合查询；时间区间统一用 [start, end+1天) 表达，避免对列做函数运算。
 */
@Mapper
public interface StatisticsMapper {

    /**
     * 平台总览：在售/待审核/完成数/成交额/浏览量/用户数/今日订单与成交额
     *
     * @return 总览统计
     */
    StatisticsOverviewVO overview();

    /**
     * 按天的浏览行为数（流量）
     *
     * @param start        开始日期（含）
     * @param end          结束日期（含）
     * @param behaviorType 行为类型，浏览传 1
     * @return 每天一行
     */
    List<TrendPoint> viewTrend(@Param("start") LocalDate start,
                               @Param("end") LocalDate end,
                               @Param("behaviorType") Integer behaviorType);

    /**
     * 按天下单数
     *
     * @param start 开始日期（含）
     * @param end   结束日期（含）
     * @return 每天一行
     */
    List<TrendPoint> orderTrend(@Param("start") LocalDate start,
                                @Param("end") LocalDate end);

    /**
     * 按天成交额（按收货时间归集已完成订单）
     *
     * @param start 开始日期（含）
     * @param end   结束日期（含）
     * @return 每天一行
     */
    List<TrendPoint> amountTrend(@Param("start") LocalDate start,
                                 @Param("end") LocalDate end);

    /**
     * 商品状态分布
     *
     * @return 每个出现的状态一行
     */
    List<ProductStatusStatVO> productStatusDistribution();

    /**
     * 分类成交额 Top N
     *
     * @param limit 取前 N 个
     * @return 按成交额倒序
     */
    List<CategorySalesVO> categorySales(@Param("limit") int limit);
}
