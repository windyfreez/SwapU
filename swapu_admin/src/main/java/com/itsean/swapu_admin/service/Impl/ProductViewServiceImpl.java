package com.itsean.swapu_admin.service.Impl;

import com.itsean.swapu_admin.service.ProductViewService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.itsean.swapu_admin.constant.StringConstant.PRODUCT_VIEW_PREFIX;

@Service
public class ProductViewServiceImpl implements ProductViewService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
