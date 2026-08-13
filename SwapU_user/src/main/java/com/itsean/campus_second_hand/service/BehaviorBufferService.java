package com.itsean.campus_second_hand.service;

import org.springframework.stereotype.Service;

@Service
public interface BehaviorBufferService {

    /**
     * 行为记录,推荐算法数据记录入Redis
     * @param userId
     * @param productId
     * @param behaviorType
     * @param categoryId
     * @param source
     */
    void recordBehavior(Long userId, Long productId, Integer behaviorType, Long categoryId, String source);
}
