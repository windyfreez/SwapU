package com.itsean.campus_second_hand.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itsean.campus_second_hand.constant.MessageConstant;
import com.itsean.campus_second_hand.constant.NumberConstant;
import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.campus_second_hand.controller.user.ChatController;
import com.itsean.campus_second_hand.dto.*;
import com.itsean.campus_second_hand.dto.*;
import com.itsean.campus_second_hand.entity.Order;
import com.itsean.campus_second_hand.entity.Product;
import com.itsean.campus_second_hand.entity.User;
import com.itsean.campus_second_hand.entity.result.PageResult;
import com.itsean.campus_second_hand.exception.OrderException;
import com.itsean.campus_second_hand.mapper.FavoriteMapper;
import com.itsean.campus_second_hand.mapper.OrderMapper;
import com.itsean.campus_second_hand.mapper.ProductMapper;
import com.itsean.campus_second_hand.mapper.UserBehaviorLogMapper;
import com.itsean.campus_second_hand.mapper.UserMapper;
import com.itsean.campus_second_hand.service.OrderService;
import com.itsean.campus_second_hand.service.ProductService;
import com.itsean.campus_second_hand.service.StockService;
import com.itsean.campus_second_hand.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private StockService stockService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private UserBehaviorLogMapper userBehaviorLogMapper;
    @Autowired
    private ChatController chatController;

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @Override
    @Transactional
    public OrderVO createOrder(OrderDTO orderDTO) {
        Order order = new Order();
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orderDTO, order);

        Long productId = orderDTO.getProductId();
        Integer needQuantity = orderDTO.getQuantity();

        //1.读 DB 实时库存（绕开热门商品缓存，避免扣减基于脏快照）
        Product product = productMapper.getProductById(productId);
        if (product == null) {
            throw new OrderException(MessageConstant.PRODUCT_NOT_EXIST);
        }
        Integer stockQuantity = product.getQuantity();

        //2.前置校验不能购买自己的商品
        Long sellerId = product.getUserId();
        Long buyerId = BaseContext.getCurrentId();
        if (buyerId.equals(sellerId)) {
            throw new OrderException(MessageConstant.CANT_BUY_YOURSELF_PRODUCT);
        }

        //3.Redis 预扣（尽力而为，异常降级为纯 DB 扣减）
        boolean redisDeducted = false;
        try {
            stockService.initIfAbsent(productId, stockQuantity);
            if (!stockService.preDeduct(productId, needQuantity)) {
                throw new OrderException(MessageConstant.NEED_MORE_THAN_STORE);
            }
            redisDeducted = true;
        } catch (OrderException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis 预扣库存异常，降级为纯 DB 扣减, productId={}", productId, e);
        }

        //4.DB 原子扣减 + 写订单，任何失败回滚 Redis 预扣
        try {
            int affected = productMapper.deductStock(productId, needQuantity, LocalDateTime.now());
            if (affected != 1) {
                throw new OrderException(MessageConstant.NEED_MORE_THAN_STORE);
            }

            //降级路径下 Redis 未预扣，扣减成功后同步 Redis 为最新值
            if (!redisDeducted) {
                try {
                    stockService.syncStock(productId, stockQuantity - needQuantity);
                } catch (Exception e) {
                    log.warn("Redis 库存同步异常，交由对账任务兜底, productId={}", productId, e);
                }
            }

            //补全订单属性（从 DB 实体取数，而非缓存快照）
            order.setProductImage(product.getImages().get(0));
            order.setProductTitle(product.getTitle());
            order.setOrderNo(generateOrderNo());
            order.setQuantity(needQuantity);
            order.setUnitPrice(product.getPrice());
            order.setAmount(product.getPrice().multiply(new BigDecimal(needQuantity)));
            order.setAddressId(orderDTO.getAddressId());
            order.setBuyerId(buyerId);
            order.setSellerId(sellerId);
            order.setStatus(Order.ORDER_STATUS_WAIT_ACCEPT);
            order.setStatusDesc(Order.ORDER_STATUS_WAIT_ACCEPT_DESC);
            order.setExpireTime(LocalDateTime.now().plusMinutes(30));
            order.setCreateTime(LocalDateTime.now());
            orderMapper.add(order);

            //库存变更后穿透热门商品缓存，避免回显旧库存/已售罄商品
            try {
                productService.evictHotCache(productId);
            } catch (Exception e) {
                log.warn("热门商品缓存失效异常, productId={}", productId, e);
            }

            //系统消息提醒卖家确认订单
            com.itsean.pojo.dto.ChatMessageDTO chatMessageDTO = new com.itsean.pojo.dto.ChatMessageDTO();
            chatMessageDTO.setToUserId(sellerId);
            chatMessageDTO.setMessageType(1);
            chatMessageDTO.setContent("有人拍下您的“" + product.getTitle() + "”，请尽快确认订单。");
            chatMessageDTO.setProductId(productId);
            chatController.systemSendMessage(chatMessageDTO);
        } catch (RuntimeException e) {
            if (redisDeducted) {
                stockService.rollbackPreDeduct(productId, needQuantity);
            }
            throw e;
        }

        BeanUtils.copyProperties(order, orderVO);
        return orderVO;
    }

    /**
     * 确认接单
     * @param orderConfirmDTO
     * @return
     */
    @Override
    public OrderConfirmVO confirmOrder(OrderConfirmDTO orderConfirmDTO) {
        String orderNo = orderConfirmDTO.getOrderNo();
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();

        //订单信息补充
        orderConfirmVO.setOrderNo(order.getOrderNo());
        orderConfirmVO.setStatus(Order.ORDER_STATUS_WAIT_PAY);
        orderConfirmVO.setStatusDesc(Order.ORDER_STATUS_WAIT_PAY_DESC);
        orderConfirmVO.setFreight(orderConfirmDTO.getFreight());
        orderConfirmVO.setConfirmTime(LocalDateTime.now());

        BeanUtils.copyProperties(orderConfirmVO,order);
        //计算订单总金额
        order.setTotalAmount(order.getAmount().add(orderConfirmDTO.getFreight()));
        orderMapper.update(order);
        return orderConfirmVO;
    }

    /**
     * 根据订单编号查询订单信息
     * @param orderNo
     * @return
     */
    @Override
    public Order getOrderByOrderNo(String orderNo) {
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        Integer status = order.getStatus();
        if (Order.ORDER_STATUS_WAIT_ACCEPT.equals(status)) {
            order.setStatusDesc(Order.ORDER_STATUS_WAIT_ACCEPT_DESC);
        } else if (Order.ORDER_STATUS_WAIT_PAY.equals(status)) {
            order.setStatusDesc(Order.ORDER_STATUS_WAIT_PAY_DESC);
        } else if (Order.ORDER_STATUS_WAIT_DELIVER.equals(status)) {
            order.setStatusDesc(Order.ORDER_STATUS_WAIT_DELIVER_DESC);
        } else if (Order.ORDER_STATUS_WAIT_RECEIVE.equals(status)) {
            order.setStatusDesc(Order.ORDER_STATUS_WAIT_RECEIVE_DESC);
        } else if (Order.ORDER_STATUS_ALREADY_RECEIVE.equals(status)) {
            order.setStatusDesc(Order.ORDER_STATUS_ALREADY_RECEIVE_DESC);
        } else {
            order.setStatusDesc("未知状态");
        }
        return order;
    }

    /**
     * 取消订单（待确认/待支付），回补库存
     * @param orderCancelDTO
     * @return
     */
    @Override
    @Transactional
    public OrderCancelVO cancelOrder(OrderCancelDTO orderCancelDTO) {
        String orderNo = orderCancelDTO.getOrderNo();
        Order order = orderMapper.getOrderByOrderNo(orderNo);

        //待确认、待支付订单可以取消
        if (order.getStatus() != Order.ORDER_STATUS_WAIT_ACCEPT
                && order.getStatus() != Order.ORDER_STATUS_WAIT_PAY) {
            throw new OrderException(MessageConstant.ORDER_STATUS_CANT_CANCEL);
        }

        //条件取消 + 回补库存（未支付，不退款）
        if (!doCancel(order, orderCancelDTO.getCancelReason(), false)) {
            throw new OrderException(MessageConstant.ORDER_STATUS_CANT_CANCEL);
        }

        OrderCancelVO orderCancelVO = new OrderCancelVO();
        orderCancelVO.setOrderNo(orderNo);
        orderCancelVO.setStatus(Order.ORDER_STATUS_CANCEL);
        orderCancelVO.setStatusDesc(Order.ORDER_STATUS_CANCEL_DESC);
        orderCancelVO.setCancelReason(orderCancelDTO.getCancelReason());
        orderCancelVO.setCancelTime(LocalDateTime.now());
        return orderCancelVO;
    }

    /**
     * 超时取消订单：条件取消并回补库存，已支付订单（余额支付）同步退款
     * @param order
     * @param reason
     * @param needRefund
     * @return 是否真正取消了订单
     */
    @Override
    @Transactional
    public boolean cancelTimeoutOrder(Order order, String reason, boolean needRefund) {
        return doCancel(order, reason, needRefund);
    }

    /**
     * 取消订单核心逻辑：条件翻转状态（幂等）→ 回补库存 → 可选退款
     * @param order
     * @param reason
     * @param needRefund
     * @return 是否真正取消了订单
     */
    private boolean doCancel(Order order, String reason, boolean needRefund) {
        //条件取消：仅当订单仍处于预期状态时取消，影响行数==1 才继续，保证幂等
        int affected = orderMapper.cancelIfCancellable(order.getOrderNo(), order.getStatus(),
                Order.ORDER_STATUS_CANCEL, LocalDateTime.now(), reason);
        if (affected != 1) {
            return false;
        }

        //回补 DB 库存 + 状态回滚（已售出→售卖中）
        productMapper.restoreStock(order.getProductId(), order.getQuantity(), LocalDateTime.now());

        //回补 Redis 库存（尽力而为）
        try {
            stockService.restore(order.getProductId(), order.getQuantity());
        } catch (Exception e) {
            log.warn("Redis 库存回补异常，交由对账任务兜底, productId={}", order.getProductId(), e);
        }

        //穿透热门商品缓存（售罄恢复可售后重新回源）
        try {
            productService.evictHotCache(order.getProductId());
        } catch (Exception e) {
            log.warn("热门商品缓存失效异常, productId={}", order.getProductId(), e);
        }

        //已支付订单（余额支付）退款
        if (needRefund && Order.PAY_TYPE_BALANCE.equals(order.getPayType())) {
            refundBalance(order);
        }
        return true;
    }

    /**
     * 余额退款：买家加回，卖家扣回（允许卖家余额为负）
     * @param order
     */
    private void refundBalance(Order order) {
        User buyer = userMapper.findById(order.getBuyerId());
        User seller = userMapper.findById(order.getSellerId());
        buyer.setBalance(buyer.getBalance().add(order.getTotalAmount()));
        seller.setBalance(seller.getBalance().subtract(order.getTotalAmount()));
        userMapper.update(buyer);
        userMapper.update(seller);
    }

    /**
     * 订单支付
     * @param orderPayDTO
     * @return
     */
    @Override
    @Transactional
    public OrderPayVO payOrder(OrderPayDTO orderPayDTO) {
        String orderNo = orderPayDTO.getOrderNo();
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        OrderPayVO orderPayVO = new OrderPayVO();
        BigDecimal totalAmount = order.getTotalAmount();
        User user = userMapper.findById(order.getBuyerId());
        User seller = userMapper.findById(order.getSellerId());

        //提高代码健壮性再来一次状态判断
        if(order.getStatus() != Order.ORDER_STATUS_WAIT_PAY){
            throw new OrderException(MessageConstant.ORDER_STATUS_CANT_PAY);
        }

        //余额支付方式需要核对支付密码，并且扣费
        if(orderPayDTO.getPayType() == Order.PAY_TYPE_BALANCE){
            if(user.getBalance().compareTo(totalAmount) < 0){
                throw new OrderException(MessageConstant.BALANCE_NOT_ENOUGH);
            }else{
                String payPassword = DigestUtils.md5DigestAsHex(orderPayDTO.getPayPassword().getBytes());
                if(!payPassword.equals(user.getPassword())){
                    throw new OrderException(MessageConstant.PASSWORD_ERROR);
                }
                //买家扣费，卖家加钱
                user.setBalance(user.getBalance().subtract(totalAmount));
                seller.setBalance(seller.getBalance().add(totalAmount));
            }
        }

        //todo 后续需要补充微信支付和支付宝支付的接口

        order.setPayTime(LocalDateTime.now());
        order.setPayType(orderPayDTO.getPayType());
        order.setStatus(Order.ORDER_STATUS_WAIT_DELIVER);

        //将更改信息写入数据库中
        orderMapper.update(order);
        userMapper.update(user);
        userMapper.update(seller);
        BeanUtils.copyProperties(order,orderPayVO);
        orderPayVO.setStatusDesc(Order.ORDER_STATUS_WAIT_DELIVER_DESC);
        return orderPayVO;
    }

    /**
     * 订单发货
     * @param orderDeliverDTO
     * @return
     */
    @Override
    public OrderDeliverVO deliverOrder(OrderDeliverDTO orderDeliverDTO) {
        String orderNo = orderDeliverDTO.getOrderNo();
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        OrderDeliverVO orderDeliverVO = new OrderDeliverVO();

        //提高代码健壮性再来一次状态判断
        if(order.getStatus() != Order.ORDER_STATUS_WAIT_DELIVER){
            throw new OrderException(MessageConstant.ORDER_STATUS_CANT_DELIVER);
        }
        BeanUtils.copyProperties(orderDeliverDTO,order);
        //检验该订单的交货方式是面交还是快递和外卖跑腿
        if(order.getDeliveryMethod() == Order.DELIVERY_METHOD_SELF_PICKUP){
            //如果该订单是面交方式，无需发货和收货，直接修改订单为已完成
            order.setStatus(Order.ORDER_STATUS_ALREADY_RECEIVE);
            order.setReceiveTime(LocalDateTime.now());
            orderDeliverVO.setStatusDesc(Order.ORDER_STATUS_ALREADY_RECEIVE_DESC);
        }else{
            //如果是外卖跑腿和快递方式，修改订单为待收货
            order.setStatus(Order.ORDER_STATUS_WAIT_RECEIVE);
            orderDeliverVO.setStatusDesc(Order.ORDER_STATUS_WAIT_RECEIVE_DESC);
        }

        order.setDeliverTime(LocalDateTime.now());
        orderMapper.update(order);
        BeanUtils.copyProperties(order,orderDeliverVO);
        return orderDeliverVO;
    }

    /**
     * 确认收货
     * @param orderReceiveDTO
     * @return
     */
    @Override
    public OrderReceiveVO receiveOrder(OrderReceiveDTO orderReceiveDTO) {
        String orderNo = orderReceiveDTO.getOrderNo();
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        OrderReceiveVO orderReceiveVO = new OrderReceiveVO();

        if(order.getStatus() != Order.ORDER_STATUS_WAIT_RECEIVE){
            throw new OrderException(MessageConstant.ORDER_STATUS_CANT_RECEIVE);
        }
        //补全属性
        orderReceiveVO.setReceiveTime(LocalDateTime.now());
        orderReceiveVO.setStatus(Order.ORDER_STATUS_ALREADY_RECEIVE);
        orderReceiveVO.setStatusDesc(Order.ORDER_STATUS_ALREADY_RECEIVE_DESC);
        orderReceiveVO.setOrderNo(orderNo);

        BeanUtils.copyProperties(orderReceiveVO,order);
        orderMapper.update(order);
        return orderReceiveVO;
    }

    /**
     * 获取订单详情
     * @param orderNo
     * @return
     */
    @Override
    public OrderDetailVO getOrderDetail(String orderNo) {
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        OrderDetailVO orderDetailVO = new OrderDetailVO();
        Long productId = order.getProductId();
        Long sellerId = order.getSellerId();
        Long buyerId = order.getBuyerId();

        User seller = userMapper.findById(sellerId);
        User buyer = userMapper.findById(buyerId);
        Product product = productMapper.getProductById(productId);

        BeanUtils.copyProperties(order,orderDetailVO);
        orderDetailVO.setProductImage(product.getImages());
        orderDetailVO.setAddressId(order.getAddressId());
        orderDetailVO.setProductTitle(product.getTitle());
        orderDetailVO.setProductDescription(product.getDescription());
        orderDetailVO.setSellerName(seller.getUsername());
        orderDetailVO.setBuyerName(buyer.getUsername());
        orderDetailVO.setSellerPhone(seller.getPhone());
        orderDetailVO.setBuyerPhone(buyer.getPhone());
        Integer status = order.getStatus();
        if (Order.ORDER_STATUS_WAIT_ACCEPT.equals(status)) {
            orderDetailVO.setStatusDesc(Order.ORDER_STATUS_WAIT_ACCEPT_DESC);
        } else if (Order.ORDER_STATUS_WAIT_PAY.equals(status)) {
            orderDetailVO.setStatusDesc(Order.ORDER_STATUS_WAIT_PAY_DESC);
        } else if (Order.ORDER_STATUS_WAIT_DELIVER.equals(status)) {
            orderDetailVO.setStatusDesc(Order.ORDER_STATUS_WAIT_DELIVER_DESC);
        } else if (Order.ORDER_STATUS_WAIT_RECEIVE.equals(status)) {
            orderDetailVO.setStatusDesc(Order.ORDER_STATUS_WAIT_RECEIVE_DESC);
        } else if (Order.ORDER_STATUS_ALREADY_RECEIVE.equals(status)) {
            orderDetailVO.setStatusDesc(Order.ORDER_STATUS_ALREADY_RECEIVE_DESC);
        } else {
            orderDetailVO.setStatusDesc("未知状态");
        }


        return orderDetailVO;
    }

    /**
     * 获取当前用户订单列表
     * @param orderPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(OrderPageQueryDTO orderPageQueryDTO) {
        //设置默认值为我是买家
        if(orderPageQueryDTO.getRole() == null || orderPageQueryDTO.getRole().isEmpty()){
            orderPageQueryDTO.setRole(OrderPageQueryDTO.I_AM_BUYER);
        }
        PageHelper.startPage(orderPageQueryDTO.getPage(),orderPageQueryDTO.getPageSize());

        //设置当前用户id
        Long currentUserId = BaseContext.getCurrentId();
        orderPageQueryDTO.setCurrentId(currentUserId);
        //查找卖家或买家为当前用户的订单并且封装成Page
        Page<Order> page = (Page<Order>) orderMapper.pageQuery(orderPageQueryDTO);
        List<OrderPageVO> orderPageVOList = new ArrayList<>();

        //设置逐个订单信息
        page.forEach(order -> {
            OrderPageVO orderPageVO = new OrderPageVO();
            BeanUtils.copyProperties(order,orderPageVO);
            //获取交易对方用户信息
            User tradeUser;
            if(OrderPageQueryDTO.I_AM_BUYER.equals(orderPageQueryDTO.getRole())){
                tradeUser = userMapper.findById(order.getSellerId());
            }else{
                tradeUser = userMapper.findById(order.getBuyerId());
            }

            if(tradeUser != null){
                orderPageVO.setTradeUserName(tradeUser.getNickname());
                orderPageVO.setTradeUserAvatar(tradeUser.getAvatar());
            }

            //将所有信息加入List
            orderPageVOList.add(orderPageVO);
        });

        return new PageResult(page.getTotal(),orderPageVOList);
    }

    /**
     * 获取订单统计信息
     * @return
     */
    @Override
    public OrderStatisticsVO getOrderStatistics() {
        Long currentUserId = BaseContext.getCurrentId();
        Integer waitPay = orderMapper.countByStatusAndBuyerUserId(Order.ORDER_STATUS_WAIT_PAY,currentUserId);
        Integer waitReceive = orderMapper.countByStatusAndBuyerUserId(Order.ORDER_STATUS_WAIT_RECEIVE,currentUserId);
        Integer waitDeliver = orderMapper.countByStatusAndSellerUserId(Order.ORDER_STATUS_WAIT_DELIVER,currentUserId);
        Integer buyCompleted = orderMapper.countByStatusAndBuyerUserId(Order.ORDER_STATUS_ALREADY_RECEIVE,currentUserId);
        Integer sellCompleted = orderMapper.countByStatusAndSellerUserId(Order.ORDER_STATUS_ALREADY_RECEIVE,currentUserId);
        BigDecimal totalCostAmount = orderMapper.sumCostAmountByUserId(currentUserId);
        BigDecimal totalSellAmount = orderMapper.sumSellAmountByUserId(currentUserId);
        // 商品与行为统计:在售/已售/我的收藏/足迹
        Integer publishCount = productMapper.countByUserIdAndStatus(currentUserId, NumberConstant.PRODUCT_STATUS_SELLING);
        Integer soldCount = productMapper.countByUserIdAndStatus(currentUserId, NumberConstant.PRODUCT_STATUS_SOLD_OUT);
        Integer favoriteCount = favoriteMapper.countByUserId(currentUserId);
        Integer footprintCount = userBehaviorLogMapper.countByUserId(currentUserId);
        OrderStatisticsVO orderStatisticsVO = OrderStatisticsVO.builder()
                .waitPay(waitPay)
                .waitReceive(waitReceive)
                .waitDeliver(waitDeliver)
                .buyCompleted(buyCompleted)
                .sellCompleted(sellCompleted)
                .totalCostAmount(totalCostAmount)
                .totalSellAmount(totalSellAmount)
                .publishCount(publishCount)
                .soldCount(soldCount)
                .favoriteCount(favoriteCount)
                .footprintCount(footprintCount)
                .build();
        return orderStatisticsVO;
    }


    /**
     * 生成订单编号
     * @return
     */
    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNum = ThreadLocalRandom.current().nextInt(10000000, 100000000);
        return dateStr + randomNum;
    }



}
