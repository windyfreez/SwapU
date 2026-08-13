package com.itsean.campus_second_hand.mapper;

import com.itsean.pojo.entity.UserBehaviorLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserBehaviorLogMapper {

    /**
     * 批量同步用户行为到数据库
     * @param batchList
     */
    void addBehavior(List<UserBehaviorLog> batchList);
}
