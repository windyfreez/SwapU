package com.itsean.swapu_admin.mapper;

import com.itsean.swapu_admin.dto.CommentAuditPageQueryDTO;
import com.itsean.swapu_admin.entity.Comment;
import com.itsean.swapu_admin.vo.CommentAuditVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     * 根据id查询评论
     *
     * @param id 评论ID
     * @return 评论实体
     */
    @Select("select * from comment where id = #{id}")
    Comment findById(Long id);

    /**
     * 分页查询评论审核列表（关联评论人与被评论人）
     *
     * @param commentAuditPageQueryDTO 审核分页查询条件
     * @return 当前页评论审核列表
     */
    List<CommentAuditVO> auditPageQuery(CommentAuditPageQueryDTO commentAuditPageQueryDTO);

    /**
     * 仅当评论处于指定状态（待审核）时更新状态，避免并发重复审核
     *
     * @param id             评论ID
     * @param status         目标状态
     * @param expectedStatus 期望的当前状态
     * @return 受影响行数，0 表示状态已被其他请求改变
     */
    int updateStatusFromExpected(@Param("id") Long id, @Param("status") Integer status,
                                 @Param("expectedStatus") Integer expectedStatus);

}
