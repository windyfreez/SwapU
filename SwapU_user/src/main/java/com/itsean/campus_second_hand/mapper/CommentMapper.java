package com.itsean.campus_second_hand.mapper;

import com.itsean.pojo.entity.Comment;
import com.itsean.pojo.vo.CommentVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     * 新增评论
     * @param comment
     */
    void insert(Comment comment);

    /**
     * 根据id查询评论
     * @param id
     * @return
     */
    @Select("select * from comment where id = #{id}")
    Comment findById(Long id);

    /**
     * 删除当前用户发出的评论
     * @param id
     * @param senderId
     * @return
     */
    @Delete("delete from comment where id = #{id} and sender_id = #{senderId}")
    int deleteByIdAndSenderId(@Param("id") Long id, @Param("senderId") Long senderId);

    /**
     * 分页查询当前用户发出的所有评论（审核中、过审、违禁均返回）
     * @param senderId
     * @return
     */
    List<CommentVO> pageQueryBySenderId(Long senderId);

    /**
     * 分页查询某个用户收到的评论（仅返回过审评论）
     * @param receiverId
     * @param status
     * @return
     */
    List<CommentVO> pageQueryByReceiverId(@Param("receiverId") Long receiverId, @Param("status") Integer status);
}
