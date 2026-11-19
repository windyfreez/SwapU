package com.itsean.campus_second_hand.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itsean.common.constant.MessageConstant;
import com.itsean.common.constant.NumberConstant;
import com.itsean.common.constant.StringConstant;
import com.itsean.common.context.BaseContext;
import com.itsean.campus_second_hand.controller.user.ChatController;
import com.itsean.pojo.dto.*;
import com.itsean.campus_second_hand.dto.*;
import com.itsean.pojo.dto.*;
import com.itsean.campus_second_hand.dto.*;
import com.itsean.pojo.entity.Order;
import com.itsean.pojo.entity.Product;
import com.itsean.campus_second_hand.entity.StockChangeLog;
import com.itsean.pojo.entity.User;
import com.itsean.pojo.PageResult;
import com.itsean.common.exception.OrderException;
import com.itsean.campus_second_hand.mapper.FavoriteMapper;
import com.itsean.campus_second_hand.mapper.OrderMapper;
import com.itsean.campus_second_hand.mapper.ProductMapper;
import com.itsean.campus_second_hand.mapper.StockChangeLogMapper;
import com.itsean.campus_second_hand.mapper.UserBehaviorLogMapper;
import com.itsean.campus_second_hand.mapper.UserMapper;
import com.itsean.campus_second_hand.mq.StockChangeProducer;
import com.itsean.campus_second_hand.service.OrderService;
import com.itsean.campus_second_hand.service.ProductService;
import com.itsean.campus_second_hand.service.StockService;
import com.itsean.pojo.vo.*;
import com.itsean.campus_second_hand.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    @Autowired
    private StockChangeLogMapper stockChangeLogMapper;
    @Autowired
    private StockChangeProducer stockChangeProducer;

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

        //用户端下单页未收集配送方式，缺省按「快递」处理，避免订单里该字段为空导致发货分支判断含糊
        if (order.getDeliveryMethod() == null) {
            order.setDeliveryMethod(Order.DELIVERY_METHOD_EXPRESS);
        }

        Long productId = orderDTO.getProductId();
        Integer needQuantity = orderDTO.getQuantity();

        //防御性校验：即使 DTO 上的 @Min(1) 被内部调用绕过，负数/零数量也绝不允许进入下单流程，
        //否则 Redis Lua 的 DECRBY 会把库存反向加回去，且 amount = 单价 × 负数 会让订单金额为负
        if (needQuantity == null || needQuantity <= 0) {
            throw new OrderException(MessageConstant.QUANTITY_INVALID);
        }

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

        //3.Redis 预扣
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
            //捕捉库存扣减失败异常，扣减失败直接降级DB扣减
            log.warn("Redis 预扣库存异常，降级为同步 DB 扣减, productId={}", productId, e);
        }

        //4.补全订单属性（从 DB 实体取数，而非缓存快照；orderNo 供订单与扣减日志共用）
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

        try {
            if (redisDeducted) {
                //异步路径：写订单 + 写扣减日志（同一事务），提交后发 MQ 异步落库
                orderMapper.add(order);
                stockChangeLogMapper.insert(buildDeductLog(order.getOrderNo(), productId, needQuantity));
                registerStockChangeAfterCommit(order.getOrderNo(), productId, needQuantity);
            } else {
                //降级路径：同步 DB 原子扣减 + 写订单，成功后同步 Redis 为最新值
                int affected = productMapper.deductStock(productId, needQuantity, LocalDateTime.now());
                if (affected != 1) {
                    throw new OrderException(MessageConstant.NEED_MORE_THAN_STORE);
                }
                try {
                    stockService.syncStock(productId, stockQuantity - needQuantity);
                } catch (Exception e) {
                    log.warn("Redis 库存同步异常，交由对账任务兜底, productId={}", productId, e);
                }
                orderMapper.add(order);
            }

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
     * 构造扣减库存变更日志（status=0 待处理，随订单事务一并落库）
     * @param orderNo
     * @param productId
     * @param quantity
     * @return
     */
    private StockChangeLog buildDeductLog(String orderNo, Long productId, Integer quantity) {
        StockChangeLog stockChangeLog = new StockChangeLog();
        stockChangeLog.setChangeNo(orderNo + StringConstant.STOCK_CHANGE_NO_DEDUCT_SUFFIX);
        stockChangeLog.setOrderNo(orderNo);
        stockChangeLog.setProductId(productId);
        stockChangeLog.setQuantity(quantity);
        stockChangeLog.setOperationType(NumberConstant.STOCK_CHANGE_OP_DEDUCT);
        stockChangeLog.setStatus(NumberConstant.STOCK_CHANGE_STATUS_PENDING);
        LocalDateTime now = LocalDateTime.now();
        stockChangeLog.setCreateTime(now);
        stockChangeLog.setUpdateTime(now);
        return stockChangeLog;
    }

    /**
     * 事务提交后发送库存扣减消息（避免消费者先于日志落库产生竞态）
     * @param orderNo
     * @param productId
     * @param quantity
     */
    private void registerStockChangeAfterCommit(String orderNo, Long productId, Integer quantity) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    stockChangeProducer.sendDeduct(orderNo, productId, quantity);
                } catch (Exception e) {
                    log.warn("库存扣减消息发送失败，交由对账任务补投, orderNo={}", orderNo, e);
                }
            }
        });
    }

    /**
     * 确认接单
     * @param orderConfirmDTO
     * @return
     */
    @Override
    public OrderConfirmVO confirmOrder(OrderConfirmDTO orderConfirmDTO) {
        String orderNo = orderConfirmDTO.getOrderNo();
        Order order = loadOrder(orderNo);

        //只有卖家本人可以确认接单
        requireSeller(order, MessageConstant.ONLY_SELLER_CAN_CONFIRM);

        //只有待确认订单可以接单：避免重复接单把「待支付/已支付」的订单打回待支付
        if (!Order.ORDER_STATUS_WAIT_ACCEPT.equals(order.getStatus())) {
            throw new OrderException(MessageConstant.ORDER_STATUS_CANT_CONFIRM);
        }

        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();
        //运费允许不传，缺省按 0 处理，避免 amount.add(null) 抛空指针
        BigDecimal freight = orderConfirmDTO.getFreight() == null
                ? BigDecimal.ZERO : orderConfirmDTO.getFreight();

        //订单信息补充
        orderConfirmVO.setOrderNo(order.getOrderNo());
        orderConfirmVO.setStatus(Order.ORDER_STATUS_WAIT_PAY);
        orderConfirmVO.setStatusDesc(Order.ORDER_STATUS_WAIT_PAY_DESC);
        orderConfirmVO.setFreight(freight);
        orderConfirmVO.setConfirmTime(LocalDateTime.now());

        BeanUtils.copyProperties(orderConfirmVO,order);
        //计算订单总金额
        order.setTotalAmount(order.getAmount().add(freight));
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
        Order order = loadOrder(orderNo);
        //订单详情只对买卖双方可见，防止拿着单号遍历他人订单
        requireParticipant(order);
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
        } else if (Order.ORDER_STATUS_REFUND_APPLYING.equals(status)) {
            order.setStatusDesc(Order.ORDER_STATUS_REFUND_APPLYING_DESC);
        } else if (Order.ORDER_STATUS_REFUNDED.equals(status)) {
            order.setStatusDesc(Order.ORDER_STATUS_REFUNDED_DESC);
        } else if (Order.ORDER_STATUS_CANCEL_APPLYING.equals(status)) {
            order.setStatusDesc(Order.ORDER_STATUS_CANCEL_APPLYING_DESC);
        } else {
            order.setStatusDesc("未知状态");
        }
        return order;
    }

    /**
     * 取消订单：待接单买家可直接取消；待支付需申请，卖家审核同意后才取消
     * @param orderCancelDTO
     * @return
     */
    @Override
    @Transactional
    public OrderCancelVO cancelOrder(OrderCancelDTO orderCancelDTO) {
        String orderNo = orderCancelDTO.getOrderNo();
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        if (order == null) {
            throw new OrderException(MessageConstant.ORDER_NOT_EXIST);
        }

        //只有买家本人可以取消订单
        Long currentUserId = BaseContext.getCurrentId();
        if (!currentUserId.equals(order.getBuyerId())) {
            throw new OrderException(MessageConstant.ONLY_BUYER_CAN_CANCEL);
        }

        Integer status = order.getStatus();

        //待接单：卖家尚未接单，买家无需卖家同意，直接取消并回补库存（未支付，不退款）
        if (Order.ORDER_STATUS_WAIT_ACCEPT.equals(status)) {
            if (!doCancel(order, orderCancelDTO.getCancelReason(), false)) {
                throw new OrderException(MessageConstant.ORDER_STATUS_CANT_CANCEL);
            }

            OrderCancelVO orderCancelVO = new OrderCancelVO();
            orderCancelVO.setOrderNo(orderNo);
            orderCancelVO.setStatus(Order.ORDER_STATUS_CANCEL);
            orderCancelVO.setStatusDesc(Order.ORDER_STATUS_CANCEL_DESC);
            orderCancelVO.setCancelReason(orderCancelDTO.getCancelReason());
            orderCancelVO.setCancelTime(LocalDateTime.now());
            log.info("买家{}直接取消待接单订单：{}", currentUserId, orderNo);
            return orderCancelVO;
        }

        //待支付：卖家已接单，买家需要申请，等待卖家审核同意后才真正取消
        if (Order.ORDER_STATUS_WAIT_PAY.equals(status)) {
            int affected = orderMapper.updateStatusAndReasonIfMatch(orderNo,
                    Order.ORDER_STATUS_WAIT_PAY, Order.ORDER_STATUS_CANCEL_APPLYING, orderCancelDTO.getCancelReason());
            if (affected != 1) {
                throw new OrderException(MessageConstant.ORDER_STATUS_CANT_CANCEL);
            }

            OrderCancelVO orderCancelVO = new OrderCancelVO();
            orderCancelVO.setOrderNo(orderNo);
            orderCancelVO.setStatus(Order.ORDER_STATUS_CANCEL_APPLYING);
            orderCancelVO.setStatusDesc(Order.ORDER_STATUS_CANCEL_APPLYING_DESC);
            orderCancelVO.setCancelReason(orderCancelDTO.getCancelReason());
            log.info("买家{}申请取消待支付订单，等待卖家审核：{}", currentUserId, orderNo);
            return orderCancelVO;
        }

        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(orderCancelDTO.getCancelReason());
        orderMapper.update(order);

        //待发货走退货退款，待收货/已收货不支持取消
        throw new OrderException(MessageConstant.ORDER_STATUS_CANT_CANCEL_APPLY);
    }

    /**
     * 卖家同意取消订单：流转为已取消，回补库存
     * @param orderCancelApproveDTO
     * @return
     */
    @Override
    @Transactional
    public OrderCancelVO approveCancel(OrderCancelApproveDTO orderCancelApproveDTO) {
        String orderNo = orderCancelApproveDTO.getOrderNo();
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        if (order == null) {
            throw new OrderException(MessageConstant.ORDER_NOT_EXIST);
        }

        //只有卖家本人可以审核取消申请
        Long currentUserId = BaseContext.getCurrentId();
        if (!currentUserId.equals(order.getSellerId())) {
            throw new OrderException(MessageConstant.ONLY_SELLER_CAN_AUDIT_CANCEL);
        }

        //只允许审核处于取消申请中的订单
        if (!Order.ORDER_STATUS_CANCEL_APPLYING.equals(order.getStatus())) {
            throw new OrderException(MessageConstant.ORDER_CANT_APPROVE_CANCEL);
        }

        //条件取消 + 回补库存（未支付，不退款），沿用买家申请时记录的原因
        if (!doCancel(order, order.getCancelReason(), false)) {
            throw new OrderException(MessageConstant.ORDER_CANT_APPROVE_CANCEL);
        }

        OrderCancelVO orderCancelVO = new OrderCancelVO();
        orderCancelVO.setOrderNo(orderNo);
        orderCancelVO.setStatus(Order.ORDER_STATUS_CANCEL);
        orderCancelVO.setStatusDesc(Order.ORDER_STATUS_CANCEL_DESC);
        orderCancelVO.setCancelReason(order.getCancelReason());
        orderCancelVO.setCancelTime(LocalDateTime.now());
        log.info("卖家{}同意取消订单：{}", currentUserId, orderNo);
        return orderCancelVO;
    }

    /**
     * 卖家拒绝取消订单：订单回到待支付，并清空申请时写入的取消原因
     * @param orderCancelRejectDTO
     * @return
     */
    @Override
    @Transactional
    public OrderCancelVO rejectCancel(OrderCancelRejectDTO orderCancelRejectDTO) {
        String orderNo = orderCancelRejectDTO.getOrderNo();
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        if (order == null) {
            throw new OrderException(MessageConstant.ORDER_NOT_EXIST);
        }

        //只有卖家本人可以审核取消申请
        Long currentUserId = BaseContext.getCurrentId();
        if (!currentUserId.equals(order.getSellerId())) {
            throw new OrderException(MessageConstant.ONLY_SELLER_CAN_AUDIT_CANCEL);
        }

        //只允许审核处于取消申请中的订单
        if (!Order.ORDER_STATUS_CANCEL_APPLYING.equals(order.getStatus())) {
            throw new OrderException(MessageConstant.ORDER_CANT_APPROVE_CANCEL);
        }

        //回到待支付，reason 传 null 表示清空取消原因
        int affected = orderMapper.updateStatusAndReasonIfMatch(orderNo,
                Order.ORDER_STATUS_CANCEL_APPLYING, Order.ORDER_STATUS_WAIT_PAY, null);
        if (affected != 1) {
            throw new OrderException(MessageConstant.ORDER_CANT_APPROVE_CANCEL);
        }

        OrderCancelVO orderCancelVO = new OrderCancelVO();
        orderCancelVO.setOrderNo(orderNo);
        orderCancelVO.setStatus(Order.ORDER_STATUS_WAIT_PAY);
        orderCancelVO.setStatusDesc(Order.ORDER_STATUS_WAIT_PAY_DESC);
        log.info("卖家{}拒绝取消订单，订单回到待支付：{}，原因：{}", currentUserId, orderNo, orderCancelRejectDTO.getRejectReason());
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
     * 买家申请退货退款：只允许待发货订单，流转为退货审核中，等待卖家审核
     * @param orderRefundApplyDTO
     * @return
     */
    @Override
    @Transactional
    public OrderRefundVO applyRefund(OrderRefundApplyDTO orderRefundApplyDTO) {
        String orderNo = orderRefundApplyDTO.getOrderNo();
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        if (order == null) {
            throw new OrderException(MessageConstant.ORDER_NOT_EXIST);
        }

        //只有买家本人可以发起退货退款申请
        Long currentUserId = BaseContext.getCurrentId();
        if (!currentUserId.equals(order.getBuyerId())) {
            throw new OrderException(MessageConstant.ONLY_BUYER_CAN_APPLY_REFUND);
        }

        //只允许待发货状态申请退货退款，待接单/待支付可直接取消订单，待收货/已收货不支持
        if (!Order.ORDER_STATUS_WAIT_DELIVER.equals(order.getStatus())) {
            throw new OrderException(MessageConstant.ORDER_STATUS_CANT_REFUND);
        }

        //条件流转：待发货 -> 退货审核中，影响行数为 1 才算成功，保证并发下幂等
        //退货原因写入 cancel_reason，订单详情页据此回显，卖家审核时能看到原因
        int affected = orderMapper.updateStatusAndReasonIfMatch(orderNo,
                Order.ORDER_STATUS_WAIT_DELIVER, Order.ORDER_STATUS_REFUND_APPLYING,
                orderRefundApplyDTO.getRefundReason());
        if (affected != 1) {
            throw new OrderException(MessageConstant.ORDER_STATUS_CANT_REFUND);
        }

        OrderRefundVO orderRefundVO = new OrderRefundVO();
        orderRefundVO.setOrderNo(orderNo);
        orderRefundVO.setStatus(Order.ORDER_STATUS_REFUND_APPLYING);
        orderRefundVO.setStatusDesc(Order.ORDER_STATUS_REFUND_APPLYING_DESC);
        orderRefundVO.setPayType(order.getPayType());
        orderRefundVO.setRefundAmount(order.getTotalAmount());
        orderRefundVO.setRefundReason(orderRefundApplyDTO.getRefundReason());
        log.info("买家{}申请退货退款成功，订单号：{}，原因：{}", currentUserId, orderNo, orderRefundApplyDTO.getRefundReason());
        return orderRefundVO;
    }

    /**
     * 卖家审核同意退货退款：流转为已退货退款，按支付方式退款并回补商品库存
     * @param orderRefundApproveDTO
     * @return
     */
    @Override
    @Transactional
    public OrderRefundVO approveRefund(OrderRefundApproveDTO orderRefundApproveDTO) {
        String orderNo = orderRefundApproveDTO.getOrderNo();
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        if (order == null) {
            throw new OrderException(MessageConstant.ORDER_NOT_EXIST);
        }

        //只有卖家本人可以审核退货申请
        Long currentUserId = BaseContext.getCurrentId();
        if (!currentUserId.equals(order.getSellerId())) {
            throw new OrderException(MessageConstant.ONLY_SELLER_CAN_APPROVE_REFUND);
        }

        //只允许审核处于退货审核中的订单
        if (!Order.ORDER_STATUS_REFUND_APPLYING.equals(order.getStatus())) {
            throw new OrderException(MessageConstant.ORDER_CANT_APPROVE_REFUND);
        }

        //条件流转：退货审核中 -> 已退货退款，影响行数为 1 才算成功，保证并发下幂等
        int affected = orderMapper.updateStatusIfMatch(orderNo,
                Order.ORDER_STATUS_REFUND_APPLYING, Order.ORDER_STATUS_REFUNDED);
        if (affected != 1) {
            throw new OrderException(MessageConstant.ORDER_CANT_APPROVE_REFUND);
        }

        //按支付方式退款
        refundByPayType(order);

        //退货后商品重新可售
        restoreRefundProductStock(order);

        OrderRefundVO orderRefundVO = new OrderRefundVO();
        orderRefundVO.setOrderNo(orderNo);
        orderRefundVO.setStatus(Order.ORDER_STATUS_REFUNDED);
        orderRefundVO.setStatusDesc(Order.ORDER_STATUS_REFUNDED_DESC);
        orderRefundVO.setPayType(order.getPayType());
        orderRefundVO.setRefundAmount(order.getTotalAmount());
        orderRefundVO.setRefundTime(LocalDateTime.now());
        log.info("卖家{}同意退货退款成功，订单号：{}", currentUserId, orderNo);
        return orderRefundVO;
    }

    /**
     * 卖家拒绝退货退款：订单回到待发货，库存与资金都不动
     * @param orderRefundRejectDTO
     * @return
     */
    @Override
    @Transactional
    public OrderRefundVO rejectRefund(OrderRefundRejectDTO orderRefundRejectDTO) {
        String orderNo = orderRefundRejectDTO.getOrderNo();
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        if (order == null) {
            throw new OrderException(MessageConstant.ORDER_NOT_EXIST);
        }

        //只有卖家本人可以审核退货申请
        Long currentUserId = BaseContext.getCurrentId();
        if (!currentUserId.equals(order.getSellerId())) {
            throw new OrderException(MessageConstant.ONLY_SELLER_CAN_APPROVE_REFUND);
        }

        //只允许审核处于退货审核中的订单
        if (!Order.ORDER_STATUS_REFUND_APPLYING.equals(order.getStatus())) {
            throw new OrderException(MessageConstant.ORDER_CANT_APPROVE_REFUND);
        }

        //条件流转：退货审核中 -> 待发货，卖家可继续发货
        //退货申请被拒绝，清空 cancel_reason，避免订单回到待发货后仍显示一条过期的退货原因
        int affected = orderMapper.updateStatusAndReasonIfMatch(orderNo,
                Order.ORDER_STATUS_REFUND_APPLYING, Order.ORDER_STATUS_WAIT_DELIVER, null);
        if (affected != 1) {
            throw new OrderException(MessageConstant.ORDER_CANT_APPROVE_REFUND);
        }

        OrderRefundVO orderRefundVO = new OrderRefundVO();
        orderRefundVO.setOrderNo(orderNo);
        orderRefundVO.setStatus(Order.ORDER_STATUS_WAIT_DELIVER);
        orderRefundVO.setStatusDesc(Order.ORDER_STATUS_WAIT_DELIVER_DESC);
        orderRefundVO.setPayType(order.getPayType());
        orderRefundVO.setRefundAmount(order.getTotalAmount());
        log.info("卖家{}拒绝退货退款，订单回到待发货：{}，原因：{}", currentUserId, orderNo, orderRefundRejectDTO.getRejectReason());
        return orderRefundVO;
    }

    /**
     * 按支付方式退款：余额支付买家加回、卖家扣回；微信/支付宝暂不做资金处理
     * @param order
     */
    private void refundByPayType(Order order) {
        //余额支付：原路退回，买家加回、卖家扣回
        if (Order.PAY_TYPE_BALANCE.equals(order.getPayType())) {
            refundBalance(order);
            return;
        }
        //todo 微信支付(1)、支付宝支付(2)暂不接入，不做任何资金操作，直接按退款成功返回
        log.info("订单{}支付方式为{}，暂不接入资金退款，直接提示退款成功", order.getOrderNo(), order.getPayType());
    }

    /**
     * 退货退款后回补商品库存，让商品重新可售
     * @param order
     */
    private void restoreRefundProductStock(Order order) {
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
        Order order = loadOrder(orderNo);

        //只有买家本人可以支付该订单
        requireBuyer(order, MessageConstant.ONLY_BUYER_CAN_PAY);

        OrderPayVO orderPayVO = new OrderPayVO();
        BigDecimal totalAmount = order.getTotalAmount();
        User user = userMapper.findById(order.getBuyerId());
        User seller = userMapper.findById(order.getSellerId());

        //提高代码健壮性再来一次状态判断
        if(!Order.ORDER_STATUS_WAIT_PAY.equals(order.getStatus())){
            throw new OrderException(MessageConstant.ORDER_STATUS_CANT_PAY);
        }

        //余额支付方式需要核对支付密码，并且扣费
        boolean balancePay = Order.PAY_TYPE_BALANCE.equals(orderPayDTO.getPayType());
        if(balancePay){
            //先校验支付密码与余额：任何一项不通过都直接抛异常，不会推进订单状态
            verifyPayPassword(user, orderPayDTO.getPayPassword());
            if(user.getBalance().compareTo(totalAmount) < 0){
                throw new OrderException(MessageConstant.BALANCE_NOT_ENOUGH);
            }
        }

        //todo 后续需要补充微信支付和支付宝支付的接口

        //原子占位：只有订单仍处于「待支付」时才能推进状态。
        //并发下两个支付请求只有一个能把 status 由 2 改成 3，另一个影响行数为 0 直接失败，避免重复扣款
        LocalDateTime payTime = LocalDateTime.now();
        int affected = orderMapper.payIfMatch(orderNo, Order.ORDER_STATUS_WAIT_PAY,
                orderPayDTO.getPayType(), payTime, Order.ORDER_STATUS_WAIT_DELIVER);
        if(affected != 1){
            throw new OrderException(MessageConstant.ORDER_STATUS_CANT_PAY);
        }

        if(balancePay){
            //买家扣费，卖家加钱；与状态流转处于同一事务，后续异常会整体回滚
            user.setBalance(user.getBalance().subtract(totalAmount));
            seller.setBalance(seller.getBalance().add(totalAmount));
            userMapper.update(user);
            userMapper.update(seller);
        }

        order.setPayTime(payTime);
        order.setPayType(orderPayDTO.getPayType());
        order.setStatus(Order.ORDER_STATUS_WAIT_DELIVER);

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
        Order order = loadOrder(orderNo);

        //只有卖家本人可以发货
        requireSeller(order, MessageConstant.ONLY_SELLER_CAN_DELIVER);

        OrderDeliverVO orderDeliverVO = new OrderDeliverVO();

        //提高代码健壮性再来一次状态判断
        if(!Order.ORDER_STATUS_WAIT_DELIVER.equals(order.getStatus())){
            throw new OrderException(MessageConstant.ORDER_STATUS_CANT_DELIVER);
        }
        BeanUtils.copyProperties(orderDeliverDTO,order);
        //检验该订单的交货方式是面交还是快递和外卖跑腿
        if(Order.DELIVERY_METHOD_SELF_PICKUP.equals(order.getDeliveryMethod())){
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
        Order order = loadOrder(orderNo);

        //只有买家本人可以确认收货
        requireBuyer(order, MessageConstant.ONLY_BUYER_CAN_RECEIVE);

        OrderReceiveVO orderReceiveVO = new OrderReceiveVO();

        if(!Order.ORDER_STATUS_WAIT_RECEIVE.equals(order.getStatus())){
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
        Order order = loadOrder(orderNo);

        //订单详情含真实姓名与手机号，只允许买卖双方查看
        requireParticipant(order);

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
        } else if (Order.ORDER_STATUS_REFUND_APPLYING.equals(status)) {
            orderDetailVO.setStatusDesc(Order.ORDER_STATUS_REFUND_APPLYING_DESC);
        } else if (Order.ORDER_STATUS_REFUNDED.equals(status)) {
            orderDetailVO.setStatusDesc(Order.ORDER_STATUS_REFUNDED_DESC);
        } else if (Order.ORDER_STATUS_CANCEL_APPLYING.equals(status)) {
            orderDetailVO.setStatusDesc(Order.ORDER_STATUS_CANCEL_APPLYING_DESC);
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
        //设置默认值为我是买家；role 只接受 buyer/seller，非法值按买家视角处理
        if(!OrderPageQueryDTO.I_AM_SELLER.equals(orderPageQueryDTO.getRole())){
            orderPageQueryDTO.setRole(OrderPageQueryDTO.I_AM_BUYER);
        }
        //分页参数缺省时补默认值，避免 PageHelper 拆箱空指针
        if(orderPageQueryDTO.getPage() == null){
            orderPageQueryDTO.setPage(NumberConstant.DEFAULT_PAGE);
        }
        if(orderPageQueryDTO.getPageSize() == null){
            orderPageQueryDTO.setPageSize(NumberConstant.DEFAULT_PAGE_SIZE);
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
     * 校验支付密码
     * <p>
     * 新账号：pay_password 存的是 BCrypt 密文，直接比对；
     * 历史账号：pay_password 为 NULL，按改版前逻辑用登录密码校验一次，
     * 通过后立刻把 hash 升级成 BCrypt 写入（惰性迁移，无需停机刷数据）。
     * @param user 买家
     * @param rawPayPassword 用户输入的明文支付密码
     */
    private void verifyPayPassword(User user, String rawPayPassword) {
        if (rawPayPassword == null || rawPayPassword.isEmpty()) {
            throw new OrderException(MessageConstant.PAY_PASSWORD_REQUIRED);
        }

        String stored = user.getPayPassword();
        if (stored != null && !stored.isEmpty()) {
            boolean matched;
            try {
                matched = BCrypt.checkpw(rawPayPassword, stored);
            } catch (IllegalArgumentException e) {
                //存量脏数据（不是合法 BCrypt 串）按校验失败处理，不放行
                matched = false;
            }
            if (!matched) {
                log.warn("支付密码校验失败，userId={}", user.getId());
                throw new OrderException(MessageConstant.PASSWORD_ERROR);
            }
            return;
        }

        //历史账号：尚未初始化支付密码，先用原逻辑校验登录密码
        String legacyHash = DigestUtils.md5DigestAsHex(rawPayPassword.getBytes());
        if (!legacyHash.equals(user.getPassword())) {
            log.warn("历史账号支付密码校验失败，userId={}", user.getId());
            throw new OrderException(MessageConstant.PASSWORD_ERROR);
        }
        //校验通过后升级为 BCrypt，由本方法的调用方随后 userMapper.update 落库
        user.setPayPassword(BCrypt.hashpw(rawPayPassword));
        log.info("用户{}首次余额支付，支付密码已由 MD5 升级为 BCrypt", user.getId());
    }

    /**
     * 按订单号加载订单，不存在时抛业务异常，避免各方法重复判空
     * @param orderNo 订单编号
     * @return 订单实体
     */
    private Order loadOrder(String orderNo) {
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        if (order == null) {
            throw new OrderException(MessageConstant.ORDER_NOT_EXIST);
        }
        return order;
    }

    /**
     * 校验当前登录用户是订单买家
     * @param order 订单
     * @param message 校验失败时的提示文案
     */
    private void requireBuyer(Order order, String message) {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null || !currentUserId.equals(order.getBuyerId())) {
            log.warn("越权操作被拦截：用户{}不是订单{}的买家", currentUserId, order.getOrderNo());
            throw new OrderException(message);
        }
    }

    /**
     * 校验当前登录用户是订单卖家
     * @param order 订单
     * @param message 校验失败时的提示文案
     */
    private void requireSeller(Order order, String message) {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null || !currentUserId.equals(order.getSellerId())) {
            log.warn("越权操作被拦截：用户{}不是订单{}的卖家", currentUserId, order.getOrderNo());
            throw new OrderException(message);
        }
    }

    /**
     * 校验当前登录用户是订单的买家或卖家
     * @param order 订单
     */
    private void requireParticipant(Order order) {
        Long currentUserId = BaseContext.getCurrentId();
        boolean isBuyer = currentUserId != null && currentUserId.equals(order.getBuyerId());
        boolean isSeller = currentUserId != null && currentUserId.equals(order.getSellerId());
        if (!isBuyer && !isSeller) {
            log.warn("越权查询被拦截：用户{}既不是订单{}的买家也不是卖家", currentUserId, order.getOrderNo());
            throw new OrderException(MessageConstant.CANT_VIEW_OTHERS_ORDER);
        }
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
