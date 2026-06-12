package com.itsean.campus_second_hand.task;// ... existing code ...
import com.itsean.campus_second_hand.constant.NumberConstant;
import com.itsean.campus_second_hand.entity.Product;
import com.itsean.campus_second_hand.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class HotProductTask {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ProductMapper productMapper;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void refreshHotProducts() {
        log.info("开始刷新热门商品到 Redis,{}", LocalDateTime.now());

        try {
            List<Product> hotProducts = productMapper.selectHotProducts(NumberConstant.HOT_PRODUCT_LIMIT);

            if (hotProducts == null || hotProducts.isEmpty()) {
                log.warn("未查询到热门商品数据");
                return;
            }

            String redisKey = NumberConstant.HOT_PRODUCTS_REDIS_KEY;
            redisTemplate.delete(redisKey);

            if (!hotProducts.isEmpty()) {
                redisTemplate.opsForList().leftPushAll(redisKey, hotProducts);
                log.info("热门商品刷新完成，共 {} 个商品", hotProducts.size());
            }

        } catch (Exception e) {
            log.error("刷新热门商品失败", e);
        }
    }
}
