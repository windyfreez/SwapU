package com.itsean.swapu_admin.controller.admin;

import com.itsean.pojo.Result;
import com.itsean.swapu_admin.annotation.RequirePermission;
import com.itsean.swapu_admin.constant.AdminPermission;
import com.itsean.swapu_admin.dto.TrafficTrendQueryDTO;
import com.itsean.swapu_admin.service.StatisticsService;
import com.itsean.swapu_admin.vo.CategorySalesVO;
import com.itsean.swapu_admin.vo.ProductStatusStatVO;
import com.itsean.swapu_admin.vo.StatisticsOverviewVO;
import com.itsean.swapu_admin.vo.TrafficTrendVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 平台统计接口（管理端工作台数据源）
 * <p>
 * 权限：AUDIT。工作台对三种角色都可见，所以这里用最小公共权限；
 * 成交额等经营指标目前对所有管理员可见，若需收紧可另加 STATS_VIEW 权限点。
 */
@RestController
@RequestMapping("/admin/statistics")
@RequirePermission(AdminPermission.AUDIT)
@Slf4j
@Api(tags = "平台统计接口")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 平台总览
     *
     * @return 核心经营指标
     */
    @GetMapping("/overview")
    @ApiOperation("平台总览")
    public Result<StatisticsOverviewVO> overview() {
        log.info("查询平台总览");
        return Result.success(statisticsService.overview());
    }

    /**
     * 流量趋势（按天）
     *
     * @param query 日期区间，缺省最近 7 天
     * @return 按日期升序的数据点
     */
    @GetMapping("/traffic-trend")
    @ApiOperation("流量趋势（按天）")
    public Result<List<TrafficTrendVO>> trafficTrend(TrafficTrendQueryDTO query) {
        log.info("查询流量趋势：{}", query);
        return Result.success(statisticsService.trafficTrend(query));
    }

    /**
     * 商品状态分布
     *
     * @return 各状态数量
     */
    @GetMapping("/product-status")
    @ApiOperation("商品状态分布")
    public Result<List<ProductStatusStatVO>> productStatus() {
        log.info("查询商品状态分布");
        return Result.success(statisticsService.productStatusDistribution());
    }

    /**
     * 分类销售分布（成交额 Top N）
     *
     * @param limit 取前 N，缺省 10
     * @return 按成交额倒序
     */
    @GetMapping("/category-sales")
    @ApiOperation("分类销售分布")
    public Result<List<CategorySalesVO>> categorySales(
            @ApiParam(value = "取前 N 个分类", example = "10")
            @RequestParam(required = false) Integer limit) {
        log.info("查询分类销售分布，limit：{}", limit);
        return Result.success(statisticsService.categorySales(limit));
    }
}
