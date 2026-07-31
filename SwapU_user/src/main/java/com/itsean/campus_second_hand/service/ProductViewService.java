package com.itsean.campus_second_hand.service;

import org.springframework.stereotype.Service;

@Service
public interface ProductViewService {
    /**
     * 浏览量 +1（只操作 Redis，超级快）
     */
    void incrementViewCount(Long productId);
}