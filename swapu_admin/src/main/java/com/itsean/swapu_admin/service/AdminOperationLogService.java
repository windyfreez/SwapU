package com.itsean.swapu_admin.service;

import com.itsean.pojo.PageResult;
import com.itsean.swapu_admin.constant.AdminLogAction;
import com.itsean.swapu_admin.dto.AdminOperationLogPageQueryDTO;

/**
 * 管理员操作日志服务
 * <p>
 * 记录范围：管理员管理相关的写操作（新增、修改、启停、删除、改密）。
 */
public interface AdminOperationLogService {

    /**
     * 记录一条操作日志
     * <p>
     * 操作人取自当前登录上下文；日志写入失败不会抛出异常，避免阻断业务操作。
     *
     * @param action     动作类型
     * @param targetType 对象类型，如 ADMIN
     * @param targetId   对象ID
     * @param detail     变更摘要，如 role:2->3
     */
    void record(AdminLogAction action, String targetType, Long targetId, String detail);

    /**
     * 分页查询操作日志
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(AdminOperationLogPageQueryDTO queryDTO);
}
