package com.itsean.campus_second_hand.aspect;

import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.campus_second_hand.entity.Product;
import com.itsean.campus_second_hand.mapper.ProductMapper;
import com.itsean.campus_second_hand.service.BehaviorBufferService;
import com.itsean.campus_second_hand.dto.FavoriteDTO;
import com.itsean.campus_second_hand.dto.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Aspect
@Slf4j
@Component
public class BehaviorAspect {

    @Resource
    private BehaviorBufferService behaviorBufferService;

    @Autowired
    private ProductMapper productMapper;

    //拦截ProductController下的所有方法
    @Pointcut("execution(* com.itsean.campus_second_hand.controller.user.ProductController.*(..))")
    public void productControllerPointCut() {}

    //拦截FavoriteController下的所有方法
    @Pointcut("execution(* com.itsean.campus_second_hand.controller.user.FavoriteController.*(..))")
    public void favoriteControllerPointCut() {}

    //拦截OrderController下的所有方法
    @Pointcut("execution(* com.itsean.campus_second_hand.controller.user.OrderController.*(..))")
    public void orderControllerPointCut() {}

    //方法成功后记录行为
    @AfterReturning(
            pointcut = "productControllerPointCut() || favoriteControllerPointCut() || orderControllerPointCut()",
            returning = "result"
    )
    public void recordBehavior(JoinPoint joinPoint, Object result){
        try {
            log.info("方法拦截成功,正在记录您的行为...");
            String methodName = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();

            int behaviorType;
            String source = "首页推荐";

            if(methodName.equals("addFavorite")) {
                behaviorType = 2;//收藏
            }else if(methodName.equals("createOrder")) {
                behaviorType = 3;//购买
            }else if(methodName.equals("getProductById")) {
                behaviorType = 1;//浏览
            }else {
                //不需要埋点的方法直接跳过即可
                return;
            }

            // userId：当前登录用户
            Long userId = BaseContext.getCurrentId();//BaseContext.getCurrentId()为null是因为拦截器未拦截该接口
            if(userId == null) {
                log.warn("游客未登录，跳过行为埋点 method={}", methodName);
                return;
            }
            // productId：按方法签名从入参提取
            Long productId = extractProductId(methodName, args);
            if (productId == null) {
                log.warn("未取到 productId，跳过埋点 method={}", methodName);
                return;
            }
            // categoryId：反查商品表
            Long categoryId = null;
            Product product = productMapper.getProductById(productId);
            if (product != null && product.getCategoryId() != null) {
                categoryId = product.getCategoryId().longValue();
            }

            //行为数据先写入Redis暂存,后定时任务异步写入DB
            behaviorBufferService.recordBehavior(userId, productId, behaviorType, categoryId, source);
        } catch (Exception e) {
            log.error("AOP记录用户行为埋点异常", e);
        }

    }

    /**
     * 获取productId方法
     * @param methodName
     * @param args
     * @return
     */
    private Long extractProductId(String methodName, Object[] args) {
        if (args == null || args.length == 0) return null;
        Object arg = args[0];
        if ("getProductById".equals(methodName)) {
            return (Long) arg;
        } else if ("addFavorite".equals(methodName)) {
            return ((FavoriteDTO) arg).getProductId();
        } else if ("createOrder".equals(methodName)) {
            return ((OrderDTO) arg).getProductId();
        }
        return null;
    }
}
