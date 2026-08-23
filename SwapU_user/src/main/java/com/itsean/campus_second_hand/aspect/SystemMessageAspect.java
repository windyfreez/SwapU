package com.itsean.campus_second_hand.aspect;

import com.itsean.campus_second_hand.controller.user.ChatController;
import com.itsean.campus_second_hand.entity.Order;
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
        //从拦截方法参数中取出orderNo，并从数据库取出详细信息
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        log.info("订单信息：{}",order);
        String productTitle = order.getProductTitle();
        Long productId = order.getProductId();
        Long buyerId = order.getBuyerId();
        Long sellerId = order.getSellerId();

        //系统小助手发送消息
        //创建新消息类
        com.itsean.pojo.dto.ChatMessageDTO chatMessageDTO = new com.itsean.pojo.dto.ChatMessageDTO();
        chatMessageDTO.setMessageType(1);

        //判断流程：不同流程完成给不同的用户发消息（买/卖家）
        if(methodName.equals("confirmOrder")) {
            //1.确认订单后：发给买家，催付款
            chatMessageDTO.setToUserId(buyerId);
            chatMessageDTO.setContent("您拍下的“" + productTitle + "”卖家已接单，请尽快付款。");
        }else if(methodName.equals("payOrder")) {
            //2.支付订单后：发给卖家，催发货
            chatMessageDTO.setToUserId(sellerId);
            chatMessageDTO.setContent("您的“" + productTitle + "”买家已接单，请尽快发货。");
        }else if(methodName.equals("deliverOrder")) {
            //2.订单发货后：发给买家，催收货
            chatMessageDTO.setToUserId(buyerId);
            chatMessageDTO.setContent("您拍下的“" + productTitle + "”卖家已接单，请关注物流信息。");
        }else if(methodName.equals("receiveOrder")) {
            //2.订单收货后：发给卖家，通知流程完成
            chatMessageDTO.setToUserId(sellerId);
            chatMessageDTO.setContent("买家已经收到您的“" + productTitle + "”！");
        }
        chatMessageDTO.setProductId(productId);
        chatController.systemSendMessage(chatMessageDTO);
    }
}
