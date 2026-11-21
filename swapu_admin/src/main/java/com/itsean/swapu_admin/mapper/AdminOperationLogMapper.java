package com.itsean.swapu_admin.mapper;

import com.itsean.swapu_admin.dto.AdminOperationLogPageQueryDTO;
import com.itsean.swapu_admin.entity.AdminOperationLog;
import com.itsean.swapu_admin.vo.AdminOperationLogVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 管理员操作日志 Mapper
 */
@Mapper
public interface AdminOperationLogMapper {

    /**
     * 写入一条操作日志
     *
     * @param log 日志实体
     */
    void insert(AdminOperationLog log);

    /**
     * 分页查询操作日志
     *
     * @param queryDTO 查询条件
     * @return 当前页日志
     */
    List<AdminOperationLogVO> pageQuery(AdminOperationLogPageQueryDTO queryDTO);
}
