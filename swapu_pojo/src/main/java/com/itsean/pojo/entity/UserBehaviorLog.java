package com.itsean.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorLog {
    private Long id;

    private Long userId;//用户ID

    private Long productId;//商品ID

    private Long categoryId;//商品分类（冗余，方便聚合）

    private int behaviorType;//1=浏览 2=收藏 3=购买 4=搜索点击

    private BigDecimal behaviorValue;//行为权重（停留时长归一化，0~1）

    private String source;//来源：首页推荐/搜索/分类浏览/聊天分享

    private LocalDateTime createTime;//行为时间

}
