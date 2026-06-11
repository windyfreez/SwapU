package com.itsean.campus_second_hand.task;

import com.itsean.campus_second_hand.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@EnableScheduling
public class ViewCountSyncTask {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ProductMapper productMapper;

    /**
     * 同步浏览量到数据库
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void syncViewCountToDB() {
        log.info("开始同步浏览量到数据库:{}", LocalDateTime.now());

        // 1. 获取所有商品浏览量 key
        Set<String> keys = stringRedisTemplate.keys("product:view:count:*");
        if (keys == null || keys.isEmpty()) return;

        List<HashMap<String, Long>> updateList = new ArrayList<>();

        for (String key : keys) {
            try {
                Long productId = Long.parseLong(key.replace("product:view:count:", ""));
                String countStr = stringRedisTemplate.opsForValue().get(key);
                if (countStr == null) continue;

                long count = Long.parseLong(countStr);
                if (count <= 0) continue;

                HashMap<String, Long> map = new HashMap<>();
                map.put("productId", productId);
                map.put("count", count);
                updateList.add(map);
            } catch (Exception ignored) {}
        }

        // 2. 批量更新数据库
        if (!updateList.isEmpty()) {
            productMapper.batchUpdateViewCount(updateList);
        }

        // 3. 删除 Redis 临时计数
        stringRedisTemplate.delete(keys);
        log.info("同步完成：{} 条" ,updateList.size());
    }
}