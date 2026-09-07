package com.itsean.campus_second_hand.service.impl;

import com.itsean.campus_second_hand.constant.StringConstant;
import com.itsean.campus_second_hand.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * 库存预扣减实现
 */
@Slf4j
@Service
public class StockServiceImpl implements StockService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 预扣减 Lua 脚本：原子判断并扣减，库存不足返回 0，成功返回 1
     */
    private static final String PRE_DEDUCT_LUA =
            "local stock = tonumber(redis.call('GET', KEYS[1]) or '0')\n" +
            "if stock >= tonumber(ARGV[1]) then\n" +
            "  redis.call('DECRBY', KEYS[1], ARGV[1])\n" +
            "  return 1\n" +
            "else\n" +
            "  return 0\n" +
            "end";

    private static final DefaultRedisScript<Long> PRE_DEDUCT_SCRIPT =
            new DefaultRedisScript<>(PRE_DEDUCT_LUA, Long.class);

    @Override
    public boolean preDeduct(Long productId, int quantity) {
        Long result = stringRedisTemplate.execute(PRE_DEDUCT_SCRIPT,
                Collections.singletonList(StringConstant.PRODUCT_STOCK_PREFIX + productId),
                String.valueOf(quantity));
        return result != null && result == 1L;
    }

    @Override
    public void rollbackPreDeduct(Long productId, int quantity) {
        stringRedisTemplate.opsForValue().increment(StringConstant.PRODUCT_STOCK_PREFIX + productId, quantity);
    }

    @Override
    public void restore(Long productId, int quantity) {
        stringRedisTemplate.opsForValue().increment(StringConstant.PRODUCT_STOCK_PREFIX + productId, quantity);
    }

    @Override
    public void initIfAbsent(Long productId, int quantity) {
        stringRedisTemplate.opsForValue().setIfAbsent(StringConstant.PRODUCT_STOCK_PREFIX + productId, String.valueOf(quantity));
    }

    @Override
    public void syncStock(Long productId, int quantity) {
        stringRedisTemplate.opsForValue().set(StringConstant.PRODUCT_STOCK_PREFIX + productId, String.valueOf(quantity));
    }
}
