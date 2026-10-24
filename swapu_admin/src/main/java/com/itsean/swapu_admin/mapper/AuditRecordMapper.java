package com.itsean.swapu_admin.mapper;

import com.itsean.swapu_admin.dto.AuditRecordPageQueryDTO;
import com.itsean.swapu_admin.entity.AuditRecord;
import com.itsean.swapu_admin.vo.AuditRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuditRecordMapper {

    /**
     * 新增审核流水
     *
     * @param auditRecord 审核流水实体
     */
    void insert(AuditRecord auditRecord);

    /**
     * 按条件分页查询审核流水
     *
     * @param auditRecordPageQueryDTO 分页查询条件
     * @return 当前页审核流水
     */
    List<AuditRecordVO> pageQuery(AuditRecordPageQueryDTO auditRecordPageQueryDTO);

    /**
     * 查询某个被审对象的历史审核流水
     *
     * @param type     审核类型：1商品 2评论 3头像
     * @param targetId 被审对象ID
     * @return 审核流水列表（按审核时间倒序）
     */
    List<AuditRecordVO> listByTarget(@Param("type") Integer type, @Param("targetId") Long targetId);

}
