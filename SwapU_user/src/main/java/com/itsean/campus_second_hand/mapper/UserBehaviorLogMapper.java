package com.itsean.campus_second_hand.mapper;

import com.itsean.pojo.entity.UserBehaviorLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserBehaviorLogMapper {

    /**
     * 批量同步用户行为到数据库
     * @param batchList
     */
    void addBehavior(List<UserBehaviorLog> batchList);

    /**
     * 查询用户浏览行为列表
     * @param currentId
     * @return
     */
    @Select("select * from user_behavior_log where user_id = #{currentId} and behavior_type = 1")
    List<UserBehaviorLog> batchList(Long currentId);

    /**
     * 统计当前用户浏览记录总数(足迹)
     * @param userId
     * @return
     */
    @Select("select count(*) from user_behavior_log where user_id = #{userId} and behavior_type = 1")
    Integer countByUserId(Long userId);

    @Select("select * from user_behavior_log where create_time >= DATE_SUB(NOW(), INTERVAL 90 DAY)")
    List<UserBehaviorLog> listRecentBehaviors();
}
