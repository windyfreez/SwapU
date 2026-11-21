package com.itsean.swapu_admin.controller.admin;

import com.itsean.pojo.PageResult;
import com.itsean.pojo.Result;
import com.itsean.swapu_admin.annotation.RequirePermission;
import com.itsean.swapu_admin.constant.AdminPermission;
import com.itsean.swapu_admin.dto.AdminOperationLogPageQueryDTO;
import com.itsean.swapu_admin.service.AdminOperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员操作日志接口
 * <p>
 * 需要 ADMIN_MANAGE 权限（等价于仅超级管理员可查看）。
 */
@RestController
@RequestMapping("/admin/operation-log")
@RequirePermission(AdminPermission.ADMIN_MANAGE)
@Slf4j
@Api(tags = "管理员操作日志接口")
public class AdminOperationLogController {

    @Autowired
    private AdminOperationLogService adminOperationLogService;

    /**
     * 分页查询管理员操作日志
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @ApiOperation("分页查询管理员操作日志")
    public Result<PageResult> pageQuery(AdminOperationLogPageQueryDTO queryDTO) {
        log.info("分页查询管理员操作日志：{}", queryDTO);
        return Result.success(adminOperationLogService.pageQuery(queryDTO));
    }

}
