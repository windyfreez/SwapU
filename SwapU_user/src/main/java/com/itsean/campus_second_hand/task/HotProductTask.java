package com.itsean.campus_second_hand.task;
import cn.hutool.json.JSONUtil;
import com.itsean.campus_second_hand.constant.NumberConstant;
import com.itsean.campus_second_hand.constant.StringConstant;
import com.itsean.campus_second_hand.entity.Product;
import com.itsean.campus_second_hand.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static com.itsean.campus_second_hand.constant.MessageConstant.CANT_SEARCH_HOT_PRODUCTS;

@Slf4j
@Component
public class HotProductTask {

    //@Resource
    //private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ProductMapper productMapper;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void refreshHotProducts() {
        log.info("开始刷新热门商品到 Redis,{}", LocalDateTime.now());

        try {
            List<Product> hotProducts = productMapper.selectHotProducts(NumberConstant.HOT_PRODUCT_LIMIT);

            if (hotProducts == null || hotProducts.isEmpty()) {
                log.warn(CANT_SEARCH_HOT_PRODUCTS);
                return;
            }
            String redisKey = StringConstant.HOT_PRODUCTS_REDIS_KEY;
            stringRedisTemplate.delete(redisKey);

            if (!hotProducts.isEmpty()) {
                hotProducts.forEach(hotProduct -> {
                    String key = redisKey + hotProduct.getId();
                    stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(hotProduct));
                });
                log.info("热门商品刷新完成，共 {} 个商品", hotProducts.size());
            }

        } catch (Exception e) {
            log.error("刷新热门商品失败", e);
        }
    }
}
