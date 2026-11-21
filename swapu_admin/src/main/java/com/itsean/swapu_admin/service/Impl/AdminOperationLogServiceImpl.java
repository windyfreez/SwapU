package com.itsean.swapu_admin.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itsean.common.context.BaseContext;
import com.itsean.pojo.PageResult;
import com.itsean.pojo.entity.Admin;
import com.itsean.swapu_admin.constant.AdminLogAction;
import com.itsean.swapu_admin.dto.AdminOperationLogPageQueryDTO;
import com.itsean.swapu_admin.entity.AdminOperationLog;
import com.itsean.swapu_admin.mapper.AdminMapper;
import com.itsean.swapu_admin.mapper.AdminOperationLogMapper;
import com.itsean.swapu_admin.service.AdminOperationLogService;
import com.itsean.swapu_admin.vo.AdminOperationLogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员操作日志服务实现
 */
@Service
@Slf4j
public class AdminOperationLogServiceImpl implements AdminOperationLogService {

    /**
     * 日志 detail 字段长度上限，与 admin_operation_log.detail varchar(500) 对齐
     */
    private static final int DETAIL_MAX_LENGTH = 500;

    /**
     * 分页参数默认值
     */
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private AdminOperationLogMapper adminOperationLogMapper;
    @Autowired
    private AdminMapper adminMapper;

    /**
     * 记录一条操作日志
     *
     * @param action     动作类型
     * @param targetType 对象类型
     * @param targetId   对象ID
     * @param detail     变更摘要
     */
    @Override
    public void record(AdminLogAction action, String targetType, Long targetId, String detail) {
        Long adminId = BaseContext.getCurrentId();
        // 没有登录上下文（如定时任务）或未指定动作时不记录
        if (adminId == null || action == null) {
            return;
        }

        try {
            AdminOperationLog operationLog = new AdminOperationLog();
            operationLog.setAdminId(adminId);

            // 冗余一份操作人账号：账号后续被改名/逻辑删除后，历史日志依然可读
            Admin operator = adminMapper.findById(adminId);
            operationLog.setAdminUsername(operator == null ? null : operator.getUsername());

            operationLog.setAction(action.name());
            operationLog.setTargetType(targetType);
            operationLog.setTargetId(targetId);
            operationLog.setDetail(truncate(detail));
            operationLog.setIp(currentIp());
            operationLog.setCreateTime(LocalDateTime.now());

            adminOperationLogMapper.insert(operationLog);
        } catch (Exception e) {
            // 审计日志写入失败不应阻断管理员的正常操作；
            // 生产环境应改为与业务同事务写入，或写入独立审计存储
            log.error("写入管理员操作日志失败，action：{}，targetId：{}", action, targetId, e);
        }
    }

    /**
     * 分页查询操作日志
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(AdminOperationLogPageQueryDTO queryDTO) {
        normalizePage(queryDTO);

        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<AdminOperationLogVO> logs = adminOperationLogMapper.pageQuery(queryDTO);

        // PageHelper 在真实链路上会把结果包装成 Page；这里兼容未包装的情况（如未走 MyBatis 拦截器），
        // 避免强转失败，同时让分页逻辑可以被单元测试覆盖
        long total;
        List<AdminOperationLogVO> records;
        if (logs instanceof Page) {
            Page<AdminOperationLogVO> page = (Page<AdminOperationLogVO>) logs;
            total = page.getTotal();
            records = page.getResult();
        } else {
            total = logs.size();
            records = logs;
        }

        // 动作码转中文描述，前端不必再维护一份映射
        records.forEach(vo -> vo.setActionDesc(AdminLogAction.descriptionOf(vo.getAction())));

        return new PageResult(total, records);
    }

    /**
     * 归一化分页参数，避免 PageHelper 收到 null 或超大 pageSize
     *
     * @param queryDTO 查询条件
     */
    private void normalizePage(AdminOperationLogPageQueryDTO queryDTO) {
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(DEFAULT_PAGE_NUM);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(DEFAULT_PAGE_SIZE);
        }
        if (queryDTO.getPageSize() > MAX_PAGE_SIZE) {
            queryDTO.setPageSize(MAX_PAGE_SIZE);
        }
    }

    /**
     * 截断 detail，避免超出字段长度导致整条日志写入失败
     *
     * @param detail 变更摘要
     * @return 截断后的摘要
     */
    private String truncate(String detail) {
        if (detail == null) {
            return null;
        }
        return detail.length() <= DETAIL_MAX_LENGTH
                ? detail
                : detail.substring(0, DETAIL_MAX_LENGTH - 3) + "...";
    }

    /**
     * 取当前请求的客户端 IP，优先使用代理转发头
     *
     * @return IP，非 Web 线程返回 null
     */
    private String currentIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            // X-Forwarded-For 可能是逗号分隔的链路，取第一个（真实客户端）
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
