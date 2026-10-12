package com.itsean.swapu_admin.service;

import com.itsean.pojo.PageResult;
import com.itsean.swapu_admin.dto.UserPageQueryDTO;
import com.itsean.swapu_admin.vo.UserManageVO;
import com.itsean.swapu_admin.vo.UserTradeStatisticsVO;

/**
 * 用户管理业务接口（管理端）
 */
public interface UserManageService {

    /**
     * 分页查询用户列表
     *
     * @param userPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    PageResult pageQuery(UserPageQueryDTO userPageQueryDTO);

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户ID
     * @return 用户详情（不含密码）
     */
    UserManageVO getById(Long id);

    /**
     * 启用/禁用用户账号
     *
     * @param id     用户ID
     * @param status 状态：1启用 0禁用
     */
    void updateStatus(Long id, Integer status);

    /**
     * 修改用户信用分
     *
     * @param id          用户ID
     * @param creditScore 信用分，取值范围0-100
     */
    void updateCreditScore(Long id, Integer creditScore);

    /**
     * 获取用户交易统计
     *
     * @param id 用户ID
     * @return 用户交易统计（买家/卖家两个视角）
     */
    UserTradeStatisticsVO getTradeStatistics(Long id);

}
