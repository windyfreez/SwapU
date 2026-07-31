package com.itsean.campus_second_hand.mapper;

import com.itsean.campus_second_hand.dto.OrderPageQueryDTO;
import com.itsean.campus_second_hand.entity.Order;
import com.itsean.campus_second_hand.vo.OrderPageVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    /**
     * 添加订单
     * @param order
     */
    @Insert("INSERT INTO orders (order_no, product_id, product_title, product_image, quantity, buyer_id, seller_id, unit_price, amount, freight, total_amount, status, expire_time, buyer_message, create_time, pay_type, pay_time, cancel_reason, cancel_time, logistics_company, logistics_no, deliver_time, confirm_time, receive_time, address_id, delivery_method) " +
            "VALUES (#{orderNo}, #{productId}, #{productTitle}, #{productImage}, #{quantity}, #{buyerId}, #{sellerId}, #{unitPrice}, #{amount}, #{freight}, #{totalAmount}, #{status}, #{expireTime}, #{buyerMessage}, #{createTime}, #{payType}, #{payTime}, #{cancelReason}, #{cancelTime}, #{logisticsCompany}, #{logisticsNo}, #{deliverTime}, #{confirmTime}, #{receiveTime}, #{addressId}, #{deliveryMethod})")
    void add(Order order);

    /**
     * 根据订单编号查询订单
     * @param orderNo
     * @return
     */
    @Select("select * from orders where order_no = #{orderNo}")
    Order getOrderByOrderNo(String orderNo);

    /**
     * 更新订单信息
     * @param order
     */
    void update(Order order);

    /**
     * 分页查询订单
     * @param orderPageQueryDTO
     * @return
     */
    List<Order> pageQuery(OrderPageQueryDTO orderPageQueryDTO);

    /**
     * 根据订单状态和买家编号查询订单数量
     * @param orderStatusWaitReceive
     * @param currentUserId
     * @return
     */
    @Select("select count(1) from orders where status = #{orderStatusWaitReceive} and buyer_id = #{currentUserId}")
    Integer countByStatusAndBuyerUserId(Integer orderStatusWaitReceive, Long currentUserId);

    /**
     * 根据订单状态和卖家编号查询订单数量
     * @param orderStatusWaitDeliver
     * @param currentUserId
     * @return
     */
    @Select("select count(1) from orders where status = #{orderStatusWaitDeliver} and seller_id = #{currentUserId}")
    Integer countByStatusAndSellerUserId(Integer orderStatusWaitDeliver, Long currentUserId);

    /**
     * 根据买家编号查询订单总金额
     * @param currentUserId
     * @return
     */
    @Select("select sum(total_amount) from orders where buyer_id = #{currentUserId}")
    BigDecimal sumCostAmountByUserId(Long currentUserId);

    /**
     * 根据卖家编号查询订单总金额
     * @param currentUserId
     * @return
     */
    @Select("select sum(total_amount) from orders where seller_id = #{currentUserId}")
    BigDecimal sumSellAmountByUserId(Long currentUserId);

    /**
     * 查询流程限时内未完成流程流转的僵尸单
     * @param status
     * @param time
     * @return
     */
    @Select("select * from orders where status = #{status} and expire_time < #{time}")
    List<Order> cancelOvertimeOrder(Integer status, LocalDateTime time);
}
