package com.itsean.swapu_admin.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itsean.pojo.PageResult;
import com.itsean.swapu_admin.constant.ErrorCodeConstant;
import com.itsean.swapu_admin.constant.MessageConstant;
import com.itsean.swapu_admin.constant.NumberConstant;
import com.itsean.swapu_admin.dto.UserPageQueryDTO;
import com.itsean.swapu_admin.entity.Order;
import com.itsean.swapu_admin.entity.User;
import com.itsean.swapu_admin.exception.UserManageException;
import com.itsean.swapu_admin.mapper.OrderMapper;
import com.itsean.swapu_admin.mapper.UserMapper;
import com.itsean.swapu_admin.service.UserManageService;
import com.itsean.swapu_admin.vo.UserManageVO;
import com.itsean.swapu_admin.vo.UserTradeStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserManageServiceImpl implements UserManageService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 分页查询用户列表
     *
     * @param userPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(UserPageQueryDTO userPageQueryDTO) {
        PageHelper.startPage(userPageQueryDTO.getPageNum(), userPageQueryDTO.getPageSize());

        List<User> users = userMapper.pageQuery(userPageQueryDTO);
        Page<User> page = (Page<User>) users;

        //转换为 VO 返回，避免密码等敏感字段外泄
        List<UserManageVO> userManageVOList = page.getResult().stream()
                .map(this::toUserManageVO)
                .collect(Collectors.toList());

        return new PageResult(page.getTotal(), userManageVOList);
    }

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户ID
     * @return 用户详情（不含密码）
     */
    @Override
    public UserManageVO getById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw userNotExist();
        }
        return toUserManageVO(user);
    }

    /**
     * 启用/禁用用户账号
     *
     * @param id     用户ID
     * @param status 状态：1启用 0禁用
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        //状态值校验：只允许启用(1)或禁用(0)
        if (status == null
                || (status != NumberConstant.ACCOUNT_STATUS_NORMAL && status != NumberConstant.ACCOUNT_STATUS_LOCK)) {
            throw new UserManageException(MessageConstant.USER_STATUS_ERROR, ErrorCodeConstant.USER_STATUS_ERROR);
        }
        //用户必须存在
        if (userMapper.findById(id) == null) {
            throw userNotExist();
        }

        User user = new User();
        user.setId(id);
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());

        userMapper.update(user);
        log.info("启用/禁用用户成功，userId：{}，status：{}", id, status);
    }

    /**
     * 修改用户信用分
     *
     * @param id          用户ID
     * @param creditScore 信用分，取值范围0-100
     */
    @Override
    public void updateCreditScore(Long id, Integer creditScore) {
        //信用分取值范围校验：0-100
        if (creditScore == null
                || creditScore < NumberConstant.CREDIT_SCORE_MIN
                || creditScore > NumberConstant.FULL_CREDIT_SCORE) {
            throw new UserManageException(MessageConstant.USER_CREDIT_SCORE_ERROR,
                    ErrorCodeConstant.USER_CREDIT_SCORE_ERROR);
        }
        //用户必须存在
        if (userMapper.findById(id) == null) {
            throw userNotExist();
        }

        User user = new User();
        user.setId(id);
        user.setCreditScore(creditScore);
        user.setUpdateTime(LocalDateTime.now());

        userMapper.update(user);
        log.info("修改用户信用分成功，userId：{}，creditScore：{}", id, creditScore);
    }

    /**
     * 获取用户交易统计
     *
     * @param id 用户ID
     * @return 用户交易统计（买家/卖家两个视角）
     */
    @Override
    public UserTradeStatisticsVO getTradeStatistics(Long id) {
        //用户必须存在
        if (userMapper.findById(id) == null) {
            throw userNotExist();
        }

        //作为买家统计
        Integer buyWaitPay = orderMapper.countByStatusAndBuyerUserId(Order.ORDER_STATUS_WAIT_PAY, id);
        Integer buyWaitReceive = orderMapper.countByStatusAndBuyerUserId(Order.ORDER_STATUS_WAIT_RECEIVE, id);
        Integer buyCompleted = orderMapper.countByStatusAndBuyerUserId(Order.ORDER_STATUS_ALREADY_RECEIVE, id);
        Integer buyCanceled = orderMapper.countByStatusAndBuyerUserId(Order.ORDER_STATUS_CANCEL, id);
        BigDecimal buyTotalAmount = orderMapper.sumAmountByBuyerIdAndStatus(id, Order.ORDER_STATUS_ALREADY_RECEIVE);

        //作为卖家统计
        Integer sellWaitDeliver = orderMapper.countByStatusAndSellerUserId(Order.ORDER_STATUS_WAIT_DELIVER, id);
        Integer sellCompleted = orderMapper.countByStatusAndSellerUserId(Order.ORDER_STATUS_ALREADY_RECEIVE, id);
        Integer sellCanceled = orderMapper.countByStatusAndSellerUserId(Order.ORDER_STATUS_CANCEL, id);
        BigDecimal sellTotalAmount = orderMapper.sumAmountBySellerIdAndStatus(id, Order.ORDER_STATUS_ALREADY_RECEIVE);

        return UserTradeStatisticsVO.builder()
                .buyWaitPay(buyWaitPay)
                .buyWaitReceive(buyWaitReceive)
                .buyCompleted(buyCompleted)
                .buyCanceled(buyCanceled)
                .buyTotalAmount(buyTotalAmount)
                .sellWaitDeliver(sellWaitDeliver)
                .sellCompleted(sellCompleted)
                .sellCanceled(sellCanceled)
                .sellTotalAmount(sellTotalAmount)
                .build();
    }

    /**
     * 实体转 VO，密码字段不复制
     *
     * @param user 用户实体
     * @return 用户列表/详情数据
     */
    private UserManageVO toUserManageVO(User user) {
        UserManageVO userManageVO = new UserManageVO();
        BeanUtils.copyProperties(user, userManageVO);
        return userManageVO;
    }

    /**
     * 构造用户不存在异常
     *
     * @return 用户管理模块业务异常
     */
    private UserManageException userNotExist() {
        return new UserManageException(MessageConstant.USER_NOT_EXIST, ErrorCodeConstant.USER_NOT_EXIST);
    }

}
