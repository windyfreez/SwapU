package com.itsean.campus_second_hand.service;

import com.itsean.campus_second_hand.entity.Product;

import java.util.List;

/**
 * 推荐商品语义重排序服务
 */
public interface ReRankService {

    /**
     * 使用大模型对候选商品做语义重排序，任何异常都回退为原顺序
     * @param candidates 待重排候选商品（已按召回分粗排）
     * @param interestCategoryIds 用户兴趣分类 id（按兴趣度降序），为空则不做重排
     * @return 重排后的商品列表
     */
    List<Product> reRank(List<Product> candidates, List<Long> interestCategoryIds);
}
