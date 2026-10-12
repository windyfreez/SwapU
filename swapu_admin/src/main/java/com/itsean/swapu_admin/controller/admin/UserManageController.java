package com.itsean.swapu_admin.controller.admin;

import com.itsean.pojo.PageResult;
import com.itsean.pojo.Result;
import com.itsean.swapu_admin.dto.UserCreditScoreDTO;
import com.itsean.swapu_admin.dto.UserPageQueryDTO;
import com.itsean.swapu_admin.dto.UserStatusDTO;
import com.itsean.swapu_admin.service.UserManageService;
import com.itsean.swapu_admin.vo.UserManageVO;
import com.itsean.swapu_admin.vo.UserTradeStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理接口（管理端）
 */
@RestController
@RequestMapping("/admin/user")
@Slf4j
@Api(tags = "用户管理接口")
public class UserManageController {

    @Autowired
    private UserManageService userManageService;

    /**
     * 分页查询用户列表
     *
     * @param userPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @ApiOperation("分页查询用户")
    public Result<PageResult> pageQuery(UserPageQueryDTO userPageQueryDTO) {
        log.info("分页查询用户：{}", userPageQueryDTO);
        PageResult pageResult = userManageService.pageQuery(userPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户ID
     * @return 用户详情（不含密码）
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询用户详情")
    public Result<UserManageVO> getById(@ApiParam(value = "用户ID", example = "1") @PathVariable Long id) {
        log.info("根据id查询用户详情：id={}", id);
        UserManageVO userManageVO = userManageService.getById(id);
        return Result.success(userManageVO);
    }

    /**
     * 启用/禁用用户账号
     *
     * @param id            用户ID
     * @param userStatusDTO 目标状态
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @ApiOperation("启用/禁用用户")
    public Result updateStatus(@ApiParam(value = "用户ID", example = "1") @PathVariable Long id,
                               @RequestBody UserStatusDTO userStatusDTO) {
        log.info("启用/禁用用户：{}，状态：{}", id, userStatusDTO.getStatus());
        userManageService.updateStatus(id, userStatusDTO.getStatus());
        return Result.success("用户状态修改成功");
    }

    /**
     * 修改用户信用分
     *
     * @param id                 用户ID
     * @param userCreditScoreDTO 目标信用分
     * @return 操作结果
     */
    @PutMapping("/{id}/credit-score")
    @ApiOperation("修改用户信用分")
    public Result updateCreditScore(@ApiParam(value = "用户ID", example = "1") @PathVariable Long id,
                                    @RequestBody UserCreditScoreDTO userCreditScoreDTO) {
        log.info("修改用户信用分：{}，信用分：{}", id, userCreditScoreDTO.getCreditScore());
        userManageService.updateCreditScore(id, userCreditScoreDTO.getCreditScore());
        return Result.success("信用分修改成功");
    }

    /**
     * 获取用户交易统计
     *
     * @param id 用户ID
     * @return 用户交易统计
     */
    @GetMapping("/{id}/trade-statistics")
    @ApiOperation("获取用户交易统计")
    public Result<UserTradeStatisticsVO> getTradeStatistics(@ApiParam(value = "用户ID", example = "1") @PathVariable Long id) {
        log.info("查询用户交易统计：id={}", id);
        UserTradeStatisticsVO statisticsVO = userManageService.getTradeStatistics(id);
        return Result.success(statisticsVO);
    }

}
