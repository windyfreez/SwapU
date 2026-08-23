package com.itsean.campus_second_hand.aspect;

import cn.hutool.db.sql.SqlBuilder;
import com.itsean.campus_second_hand.controller.user.ChatController;
import com.itsean.campus_second_hand.entity.Order;
import com.itsean.campus_second_hand.mapper.ChatMapper;
import com.itsean.campus_second_hand.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;

@Aspect
@Slf4j
@Component
public class SystemMessageAspect {

    @Resource
    private OrderMapper orderMapper;
    @Autowired
    private ChatController chatController;

    @Pointcut("execution(* com.itsean.campus_second_hand.controller.user.OrderController.payOrder(..)) ||" +
            "execution(* com.itsean.campus_second_hand.controller.user.OrderController.deliverOrder(..)) ||" +
            "execution(* com.itsean.campus_second_hand.controller.user.OrderController.receiveOrder(..)) ||" +
            "execution(* com.itsean.campus_second_hand.controller.user.OrderController.confirmOrder(..))")
    public void orderControllerPointCut() {}

    @AfterReturning(
            pointcut = "orderControllerPointCut()",
            returning = "result"

    )
    public void systemSendMessage(JoinPoint joinPoint, Object result) {
        log.info("方法拦截成功，系统小助手正在发送消息...");
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();

        String orderNo = null;

        for(Object arg : args) {
            if (arg == null) {
                continue;
            }
            try {
                Field field = arg.getClass().getDeclaredField("orderNo");
                field.setAccessible(true);
                orderNo = (String) field.get(arg);
                break;

            } catch (NoSuchFieldException | IllegalAccessException e) {
                // 当前参数没有 orderNo，继续找下一个参数
            }
        }
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        log.info("订单信息：{}",order);
        String productTitle = order.getProductTitle();
        Long productId = order.getProductId();
        Long buyerId = order.getBuyerId();
        Long sellerId = order.getSellerId();

        //系统小助手发送消息
        com.itsean.pojo.dto.ChatMessageDTO chatMessageDTO = new com.itsean.pojo.dto.ChatMessageDTO();
        chatMessageDTO.setMessageType(1);
        //判断流程：不同流程完成给不同的用户发消息（买/卖家）
        if(methodName.equals("confirmOrder")){
            chatMessageDTO.setToUserId(sellerId);
            chatMessageDTO.setContent("有人拍下您的“" + productTitle + "”，请尽快确认订单。");
            chatMessageDTO.setProductId(productId);
            chatController.systemSendMessage(chatMessageDTO);
        }

    }
}
