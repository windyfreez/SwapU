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
            "execution(* com.itsean.campus_second_hand.controller.user.OrderController.confirmOrder(..)) ||" +
            "execution(* com.itsean.campus_second_hand.controller.user.OrderController.applyRefund(..)) ||" +
            "execution(* com.itsean.campus_second_hand.controller.user.OrderController.approveRefund(..)) ||" +
            "execution(* com.itsean.campus_second_hand.controller.user.OrderController.rejectRefund(..)) ||" +
            "execution(* com.itsean.campus_second_hand.controller.user.OrderController.cancelOrder(..)) ||" +
            "execution(* com.itsean.campus_second_hand.controller.user.OrderController.approveCancel(..)) ||" +
            "execution(* com.itsean.campus_second_hand.controller.user.OrderController.rejectCancel(..))")
    public void orderControllerPointCut() {}

    @AfterReturning(
            pointcut = "orderControllerPointCut()",
            returning = "result"

    )
    public void systemSendMessage(JoinPoint joinPoint, Object result) {
        log.info("方法拦截成功，系统小助手正在发送消息...");
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();

        //从拦截方法参数中取出订单编号与拒绝原因
        String orderNo = extractStringArg(args, "orderNo");
        String rejectReason = extractStringArg(args, "rejectReason");
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
            chatMessageDTO.setContent("您的“" + productTitle + "”买家已支付，请尽快发货。");
        }else if(methodName.equals("deliverOrder")) {
            //2.订单发货后：发给买家，催收货
            chatMessageDTO.setToUserId(buyerId);
            chatMessageDTO.setContent("您拍下的“" + productTitle + "”卖家已接单，请关注物流信息。");
        }else if(methodName.equals("receiveOrder")) {
            //2.订单收货后：发给卖家，通知流程完成
            chatMessageDTO.setToUserId(sellerId);
            chatMessageDTO.setContent("买家已经收到您的“" + productTitle + "”！");
        }else if(methodName.equals("applyRefund")) {
            //3.买家申请退货退款后：发给卖家，提醒审核是否同意退货
            chatMessageDTO.setToUserId(sellerId);
            chatMessageDTO.setContent("买家对您的“" + productTitle + "”申请了退货退款，请您及时审核是否同意退货。");
        }else if(methodName.equals("approveRefund")) {
            //4.卖家同意退货退款后：发给买家，告知退款成功
            chatMessageDTO.setToUserId(buyerId);
            chatMessageDTO.setContent("您申请的“" + productTitle + "”退货退款已审核通过，退款成功。");
        }else if(methodName.equals("rejectRefund")) {
            //5.卖家拒绝退货退款后：发给买家
            chatMessageDTO.setToUserId(buyerId);
            chatMessageDTO.setContent("卖家拒绝了您对“" + productTitle + "”的退货退款申请" + buildReasonSuffix(rejectReason) + "。");
        }else if(methodName.equals("cancelOrder")) {
            //6.取消订单：待接单买家直接取消则告知卖家；待支付买家申请取消则提醒卖家审核
            chatMessageDTO.setToUserId(sellerId);
            if(Order.ORDER_STATUS_CANCEL.equals(order.getStatus())) {
                chatMessageDTO.setContent("买家已取消订单：“" + productTitle + "”。");
            }else{
                chatMessageDTO.setContent("买家申请取消“" + productTitle + "”订单，请您及时审核是否同意。");
            }
        }else if(methodName.equals("approveCancel")) {
            //7.卖家同意取消订单后：发给买家
            chatMessageDTO.setToUserId(buyerId);
            chatMessageDTO.setContent("卖家已同意取消“" + productTitle + "”订单，订单已取消。");
        }else if(methodName.equals("rejectCancel")) {
            //8.卖家拒绝取消订单后：发给买家
            chatMessageDTO.setToUserId(buyerId);
            chatMessageDTO.setContent("卖家拒绝了您取消“" + productTitle + "”订单的申请，订单继续有效" + buildReasonSuffix(rejectReason) + "。");
        }
        chatMessageDTO.setProductId(productId);
        chatController.systemSendMessage(chatMessageDTO);
    }

    /**
     * 从方法入参中按字段名取值（参数没有该字段或取不到值时返回 null）
     * @param args
     * @param fieldName
     * @return
     */
    private String extractStringArg(Object[] args, String fieldName) {
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            try {
                Field field = arg.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(arg);
                if (value != null) {
                    return value.toString();
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // 当前参数没有该字段，继续找下一个参数
            }
        }
        return null;
    }

    /**
     * 拼装拒绝原因，没有原因时返回空串
     * @param rejectReason
     * @return
     */
    private String buildReasonSuffix(String rejectReason) {
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            return "";
        }
        return "，拒绝原因：" + rejectReason;
    }
}
