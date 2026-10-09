package com.itsean.campus_second_hand.mapper;

import com.itsean.campus_second_hand.entity.StockChangeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface StockChangeLogMapper {

    /**
     * 新增库存变更日志（change_no 唯一键幂等）
     * @param stockChangeLog
     */
    void insert(StockChangeLog stockChangeLog);

    /**
     * 根据流水号查询日志（幂等判断）
     * @param changeNo
     * @return
     */
    StockChangeLog getByChangeNo(String changeNo);

    /**
     * 更新日志状态
     * @param changeNo
     * @param status
     * @param updateTime
     */
    void updateStatus(@Param("changeNo") String changeNo,
                      @Param("status") Integer status,
                      @Param("updateTime") LocalDateTime updateTime);

    /**
     * 查询超时未落库的待处理扣减日志（用于补投）
     * @param beforeTime
     * @return
     */
    List<StockChangeLog> selectPending(LocalDateTime beforeTime);

    /**
     * 按商品分组统计未落库的扣减量（用于对账校准 Redis）
     * @return
     */
    List<Map<String, Object>> selectPendingDeductGroupByProduct();
}
