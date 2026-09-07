package com.itsean.campus_second_hand.service;

/**
 * 库存预扣减服务：Redis 预扣（加速）+ DB 原子扣减兜底
 */
public interface StockService {

    /**
     * 预扣库存（Redis 原子操作），成功返回 true，库存不足返回 false
     * @param productId
     * @param quantity
     * @return
     */
    boolean preDeduct(Long productId, int quantity);

    /**
     * 回滚预扣（DB 扣减失败时补偿）
     * @param productId
     * @param quantity
     */
    void rollbackPreDeduct(Long productId, int quantity);

    /**
     * 回补库存（取消订单时）
     * @param productId
     * @param quantity
     */
    void restore(Long productId, int quantity);

    /**
     * 初始化库存 key（不存在时写入）
     * @param productId
     * @param quantity
     */
    void initIfAbsent(Long productId, int quantity);

    /**
     * 以 DB 为准同步单个商品库存到 Redis
     * @param productId
     * @param quantity
     */
    void syncStock(Long productId, int quantity);
}
