package com.itsean.campus_second_hand.controller.user;

import com.itsean.campus_second_hand.dto.*;
import com.itsean.campus_second_hand.entity.Order;
import com.itsean.campus_second_hand.entity.result.PageResult;
import com.itsean.campus_second_hand.entity.result.Result;
import com.itsean.campus_second_hand.service.OrderService;
import com.itsean.campus_second_hand.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Slf4j
@Api(tags = "订单接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @PostMapping("/create")
    @ApiOperation("创建订单")
    public Result<OrderVO> createOrder(@RequestBody OrderDTO orderDTO){
        log.info("创建订单：{}",orderDTO);
        OrderVO orderVO = orderService.createOrder(orderDTO);
        return Result.success(orderVO);
    }

    /**
     * 根据orderNo查询订单
     * @param orderNo
     * @return
     */
    @GetMapping("/{orderNo}")
    @ApiOperation("根据orderNo查询订单")
    public Result<Order> getOrderByOrderNo(@PathVariable String orderNo){
        log.info("查询订单：{}",orderNo);
        Order order = orderService.getOrderByOrderNo(orderNo);
        return Result.success(order);
    }

    /**
     * 确认接单
     * @return
     */
    @PostMapping("/confirm")
    @ApiOperation("确认接单")
    public Result<OrderConfirmVO> confirmOrder(@RequestBody OrderConfirmDTO orderConfirmDTO){
        log.info("确认订单:{}",orderConfirmDTO);
        OrderConfirmVO orderConfirmVO = orderService.confirmOrder(orderConfirmDTO);
        return Result.success(orderConfirmVO);
    }

    /**
     * 取消订单
     * @param orderCancelDTO
     * @return
     */
    @PostMapping("/cancel")
    @ApiOperation("取消订单")
    public Result<OrderCancelVO> cancelOrder(@RequestBody OrderCancelDTO orderCancelDTO){
        log.info("取消订单:{}",orderCancelDTO);
        OrderCancelVO orderCancelVO = orderService.cancelOrder(orderCancelDTO);
        return Result.success(orderCancelVO);
    }

    /**
     * 支付订单
     * @param orderPayDTO
     * @return
     */
    @PostMapping("/pay")
    @ApiOperation("支付订单")
    public Result<OrderPayVO> payOrder(@RequestBody OrderPayDTO orderPayDTO){
        log.info("支付订单:{}",orderPayDTO);
        OrderPayVO orderPayVO = orderService.payOrder(orderPayDTO);
        return Result.success(orderPayVO);
    }

    /**
     * 订单发货
     * @param orderDeliverDTO
     * @return
     */
    @PostMapping("/deliver")
    @ApiOperation("订单发货")
    public Result<OrderDeliverVO> deliverOrder(@RequestBody OrderDeliverDTO orderDeliverDTO){
        log.info("发货订单:{}",orderDeliverDTO);
        OrderDeliverVO orderDeliverVO = orderService.deliverOrder(orderDeliverDTO);
        return Result.success(orderDeliverVO);
    }

    /**
     * 确认收货
     * @param orderReceiveDTO
     * @return
     */
    @PostMapping("/receive")
    @ApiOperation("确认收货")
    public Result<OrderReceiveVO> receiveOrder(@RequestBody OrderReceiveDTO orderReceiveDTO){
        log.info("确认收货:{}",orderReceiveDTO);
        OrderReceiveVO orderReceiveVO = orderService.receiveOrder(orderReceiveDTO);
        return Result.success(orderReceiveVO);
    }

    /**
     * 查询订单详情
     * @param orderNo
     * @return
     */
    @GetMapping("/detail/{orderNo}")
    @ApiOperation("查询订单详情")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable String orderNo){
        log.info("查询订单详情:{}",orderNo);
        OrderDetailVO orderDetailVO = orderService.getOrderDetail(orderNo);
        return Result.success(orderDetailVO);
    }

    /**
     * 分页获取当前用户所有订单
     * @param orderPageQueryDTO
     * @return
     */
    @ApiOperation("分页获取当前用户所有订单")
    @PostMapping("/pageQuery")
    public Result<PageResult> pageQuery(OrderPageQueryDTO orderPageQueryDTO){
    	log.info("分页获取当前用户所有订单：{}",orderPageQueryDTO);
    	PageResult pageResult = orderService.pageQuery(orderPageQueryDTO);
    	return Result.success(pageResult);
    }

    /**
     * 获取订单统计信息
     * @return
     */
    @ApiOperation("获取订单统计信息")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> getOrderStatistics(){
        log.info("获取订单统计信息");
        OrderStatisticsVO orderStatisticsVO = orderService.getOrderStatistics();
        return Result.success(orderStatisticsVO);
    }



}
