package com.itsean.campus_second_hand.task;

import com.itsean.campus_second_hand.mapper.UserBehaviorLogMapper;
import com.itsean.pojo.entity.UserBehaviorLog;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.itsean.campus_second_hand.constant.StringConstant.USER_PROFILE_REDIS_KEY_PREFIX;

@Component
public class UserProfileTask {
    @Resource
    private UserBehaviorLogMapper behaviorLogMapper;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 个人画像重建定时任务
     */
    @Scheduled(cron = "0 0/10 * * * *")
    public void rebuildProfiles() {
        List<UserBehaviorLog> logs = behaviorLogMapper.listRecentBehaviors();
        Map<Long, Map<Long, Double>> profiles = new HashMap<>();
        for (UserBehaviorLog log : logs) {
            //先进行非空判断
            if (log.getUserId() == null || log.getCategoryId() == null) {
                continue;
            }

            //用户行为权重
            double base = behaviorWeight(log.getBehaviorType());
            //行为权重（可有可无）
            double value = log.getBehaviorValue() == null ? 1D : log.getBehaviorValue().doubleValue();
            //经过天数，用于计算衰减系数
            long days = log.getCreateTime() == null ? 0 :
                    Math.max(0, Duration.between(log.getCreateTime(), LocalDateTime.now()).toDays());
            //衰减系数
            double decay = Math.pow(0.5D, days / 30D);
            //量化用户兴趣————兴趣系数
            profiles.computeIfAbsent(log.getUserId(), k -> new HashMap<>())
                    .merge(log.getCategoryId(), base * value * decay, Double::sum);
        }

        profiles.forEach((userId, categories) -> {
            //先删除旧缓存
            String key = USER_PROFILE_REDIS_KEY_PREFIX + userId;
            stringRedisTemplate.delete(key);

            //记录每个类别的加权分，并存入Redis中便于多路召回实现
            Map<String, String> values = new HashMap<>();
            categories.forEach((categoryId, score) ->
                    values.put(String.valueOf(categoryId), String.valueOf(score)));

            //另起一行哈希记录时间戳，key:_updatedAt; value:currentTime
            values.put("_updatedAt", String.valueOf(System.currentTimeMillis()));
            stringRedisTemplate.opsForHash().putAll(key, values);
        });
    }

    //计算不同行为的行为权重
    private double behaviorWeight(int type) {
        switch (type) {
            case 2: return 4D;
            case 3: return 6D;
            case 4: return 2D;
            default: return 1D;
        }
    }
}
