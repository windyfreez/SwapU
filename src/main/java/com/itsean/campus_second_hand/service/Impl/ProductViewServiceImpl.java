package com.itsean.campus_second_hand.service.Impl;

import com.itsean.campus_second_hand.service.ProductViewService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProductViewServiceImpl implements ProductViewService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

     private static final String PRODUCT_VIEW_PREFIX = "product:view:count:";

    /**
     * 浏览量 +1（只操作 Redis，超级快）
     * @param productId
     */
    @Override
    public void incrementViewCount(Long productId) {
        String key = PRODUCT_VIEW_PREFIX + productId;
        stringRedisTemplate.opsForValue().increment(key, 1);
    }
}
