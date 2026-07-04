package com.itsean.campus_second_hand.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itsean.campus_second_hand.constant.MessageConstant;
import com.itsean.campus_second_hand.constant.NumberConstant;
import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.campus_second_hand.dto.*;
import com.itsean.campus_second_hand.entity.Order;
import com.itsean.campus_second_hand.entity.Product;
import com.itsean.campus_second_hand.entity.User;
import com.itsean.campus_second_hand.entity.result.PageResult;
import com.itsean.campus_second_hand.exception.OrderException;
import com.itsean.campus_second_hand.mapper.OrderMapper;
import com.itsean.campus_second_hand.mapper.ProductMapper;
import com.itsean.campus_second_hand.mapper.UserMapper;
import com.itsean.campus_second_hand.service.OrderService;
import com.itsean.campus_second_hand.service.ProductService;
import com.itsean.campus_second_hand.vo.*;
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

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

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
        BeanUtils.copyProperties(orderDTO,order);

        //获取商品信息并且更改商品状态
        Long productId = orderDTO.getProductId();
        ProductDetailVO productDetail = productService.getProductById(productId);

        //补全商品标题和封面图属性
        order.setProductImage(productDetail.getImages().get(0));
        order.setProductTitle(productDetail.getTitle());
        order.setOrderNo(generateOrderNo());

        //计算商品价格并补充属性
        BigDecimal unitPrice = productDetail.getPrice();
        Integer needQuantity = orderDTO.getQuantity();
        Integer stockQuantity = productDetail.getQuantity();
        //判断库存是否充足
        if (stockQuantity < needQuantity) {
            throw new OrderException(MessageConstant.NEED_MORE_THAN_STORE);
        }else{
            order.setQuantity(needQuantity);
            productDetail.setQuantity(stockQuantity - needQuantity);
        }
        order.setUnitPrice(unitPrice);
        order.setAmount(unitPrice.multiply(new BigDecimal(needQuantity)));
        productDetail.setQuantity(stockQuantity - needQuantity);
        //扣减商品库存，更改商品状态
        if(stockQuantity == needQuantity){
            productDetail.setStatus(NumberConstant.PRODUCT_STATUS_SOLD_OUT);
        }else{
            //还有库存，还可以出售
            productDetail.setStatus(NumberConstant.PRODUCT_STATUS_SELLING);
        }

        //补全其他属性
        order.setAddressId(orderDTO.getAddressId());
        order.setStatus(Order.ORDER_STATUS_WAIT_ACCEPT);
        order.setStatusDesc(Order.ORDER_STATUS_WAIT_ACCEPT_DESC);
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));
        order.setCreateTime(LocalDateTime.now());
        if(!BaseContext.getCurrentId().equals(productDetail.getSellerInfo().getId())){
            order.setBuyerId(BaseContext.getCurrentId());
            order.setSellerId(productDetail.getSellerInfo().getId());
        }else{
            throw new OrderException(MessageConstant.CANT_BUY_YOURSELF_PRODUCT);
        }


        //实体类存入数据库
        Product product = new Product();
        BeanUtils.copyProperties(productDetail,product);
        productMapper.update(product);
        BeanUtils.copyProperties(order,orderVO);
        orderMapper.add(order);

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
     * 取消订单
     * @param orderCancelDTO
     * @return
     */
    @Override
    public OrderCancelVO cancelOrder(OrderCancelDTO orderCancelDTO) {
        String orderNo = orderCancelDTO.getOrderNo();
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        OrderCancelVO orderCancelVO = new OrderCancelVO();

        //判断订单状态，待确认订单和待支付订单可以取消
        if (order.getStatus() == Order.ORDER_STATUS_WAIT_ACCEPT || order.getStatus() == Order.ORDER_STATUS_WAIT_PAY) {
            order.setStatusDesc(Order.ORDER_STATUS_CANCEL_DESC);
            order.setStatus(Order.ORDER_STATUS_CANCEL);
            order.setCancelReason(orderCancelDTO.getCancelReason());
            order.setCancelTime(LocalDateTime.now());
            orderMapper.update(order);
        }else{
            throw new OrderException(MessageConstant.ORDER_STATUS_CANT_CANCEL);
        }

        BeanUtils.copyProperties(order,orderCancelVO);
        return orderCancelVO;
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
        OrderStatisticsVO orderStatisticsVO = OrderStatisticsVO.builder()
                .waitPay(waitPay)
                .waitReceive(waitReceive)
                .waitDeliver(waitDeliver)
                .buyCompleted(buyCompleted)
                .sellCompleted(sellCompleted)
                .totalCostAmount(totalCostAmount)
                .totalSellAmount(totalSellAmount)
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
