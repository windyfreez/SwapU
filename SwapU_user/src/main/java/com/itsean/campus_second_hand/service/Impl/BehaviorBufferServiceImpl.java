package com.itsean.campus_second_hand.service.Impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.itsean.campus_second_hand.service.BehaviorBufferService;
import com.itsean.pojo.entity.UserBehaviorLog;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.itsean.common.constant.StringConstant.BEHAVIOR_QUEUE_KEY;

@Service
public class BehaviorBufferServiceImpl implements BehaviorBufferService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 行为记录,推荐算法数据记录入Redis
     * @param userId
     * @param productId
     * @param behaviorType
     * @param categoryId
     * @param source
     */
    @Override
    public void recordBehavior(Long userId, Long productId, Integer behaviorType, Long categoryId, String source) {
        UserBehaviorLog userBehaviorLog = new UserBehaviorLog();
        userBehaviorLog.setUserId(userId);
        userBehaviorLog.setCategoryId(categoryId);
        userBehaviorLog.setProductId(productId);
        userBehaviorLog.setBehaviorType(behaviorType);
        userBehaviorLog.setBehaviorValue(new BigDecimal("1.0000"));
        userBehaviorLog.setSource(source);
        userBehaviorLog.setCreateTime(LocalDateTime.now());

        String logJson = JSONUtil.toJsonStr(userBehaviorLog);
        stringRedisTemplate.opsForList().rightPush(BEHAVIOR_QUEUE_KEY, logJson);

    }
}
