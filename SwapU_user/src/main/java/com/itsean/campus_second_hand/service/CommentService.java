package com.itsean.campus_second_hand.service;

import com.itsean.pojo.PageResult;
import com.itsean.pojo.dto.CommentAddDTO;
import com.itsean.pojo.dto.CommentPageQueryDTO;

public interface CommentService {

    /**
     * 新增评论
     * @param commentAddDTO
     */
    void addComment(CommentAddDTO commentAddDTO);

    /**
     * 删除评论
     * @param id
     */
    void deleteComment(Long id);

    /**
     * 分页查询当前用户发出的所有评论
     * @param commentPageQueryDTO
     * @return
     */
    PageResult pageQueryMyComments(CommentPageQueryDTO commentPageQueryDTO);

    /**
     * 分页查询某个用户收到的所有评论
     * @param receiverId
     * @param commentPageQueryDTO
     * @return
     */
    PageResult pageQueryReceivedComments(Long receiverId, CommentPageQueryDTO commentPageQueryDTO);
}
