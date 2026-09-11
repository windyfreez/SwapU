package com.itsean.campus_second_hand.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.campus_second_hand.mapper.CommentMapper;
import com.itsean.campus_second_hand.entity.User;
import com.itsean.campus_second_hand.mapper.UserMapper;
import com.itsean.campus_second_hand.service.CommentService;
import com.itsean.common.constant.MessageConstant;
import com.itsean.common.constant.NumberConstant;
import com.itsean.common.constant.StringConstant;
import com.itsean.common.exception.CommentException;
import com.itsean.pojo.PageResult;
import com.itsean.pojo.dto.CommentAddDTO;
import com.itsean.pojo.dto.CommentPageQueryDTO;
import com.itsean.pojo.entity.Comment;
import com.itsean.pojo.vo.CommentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 新增评论
     * @param commentAddDTO
     */
    @Override
    public void addComment(CommentAddDTO commentAddDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("当前用户id：{}", currentUserId);

        //校验接收者是否存在
        User receiver = userMapper.findById(commentAddDTO.getReceiverId());
        if (receiver == null) {
            throw new CommentException(MessageConstant.USER_NOT_EXIST);
        }

        //校验不能给自己评论
        if (currentUserId.equals(commentAddDTO.getReceiverId())) {
            throw new CommentException(MessageConstant.CANT_COMMENT_YOURSELF);
        }

        //校验评论文字内容不能为空
        if (commentAddDTO.getContent() == null || commentAddDTO.getContent().trim().isEmpty()) {
            throw new CommentException(MessageConstant.COMMENT_CONTENT_EMPTY);
        }

        Comment comment = new Comment();
        //发送者取自当前登录用户上下文，避免前端伪造
        comment.setSenderId(currentUserId);
        comment.setReceiverId(commentAddDTO.getReceiverId());
        comment.setImage(commentAddDTO.getImage());
        comment.setContent(commentAddDTO.getContent());
        comment.setSendTime(LocalDateTime.now());
        //新评论统一为审核中状态，由管理端审核后流转为过审或违禁
        comment.setStatus(NumberConstant.COMMENT_STATUS_CHECKING);
        //评论类型不传时默认为好评
        comment.setCommentType(commentAddDTO.getCommentType() != null ? commentAddDTO.getCommentType() : NumberConstant.COMMENT_TYPE_GOOD);

        commentMapper.insert(comment);
        log.info("用户{}评论用户{}成功，评论id：{}", currentUserId, comment.getReceiverId(), comment.getId());
    }

    /**
     * 删除评论
     * @param id
     */
    @Override
    public void deleteComment(Long id) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("当前用户id：{}", currentUserId);

        Comment comment = commentMapper.findById(id);
        if (comment == null) {
            throw new CommentException(MessageConstant.COMMENT_NOT_EXIST);
        }
        //只能删除自己发出的评论
        if (!currentUserId.equals(comment.getSenderId())) {
            throw new CommentException(MessageConstant.CANT_DELETE_OTHERS_COMMENT);
        }

        commentMapper.deleteByIdAndSenderId(id, currentUserId);
        log.info("用户{}删除评论{}成功", currentUserId, id);
    }

    /**
     * 分页查询当前用户发出的所有评论
     * @param commentPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQueryMyComments(CommentPageQueryDTO commentPageQueryDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("当前用户id：{}", currentUserId);

        PageHelper.startPage(commentPageQueryDTO.getPage(), commentPageQueryDTO.getPageSize());

        //发出的评论不区分审核状态，审核中、过审、违禁都要回显给本人
        List<CommentVO> commentList = commentMapper.pageQueryBySenderId(currentUserId);
        commentList.forEach(this::fillDesc);

        Page<CommentVO> page = (Page<CommentVO>) commentList;
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 分页查询某个用户收到的所有评论
     * @param receiverId
     * @param commentPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQueryReceivedComments(Long receiverId, CommentPageQueryDTO commentPageQueryDTO) {
        log.info("查询用户{}收到的评论", receiverId);

        PageHelper.startPage(commentPageQueryDTO.getPage(), commentPageQueryDTO.getPageSize());

        //收到的评论只有过审评论对外可见
        List<CommentVO> commentList = commentMapper.pageQueryByReceiverId(receiverId, NumberConstant.COMMENT_STATUS_APPROVED);
        commentList.forEach(this::fillDesc);

        Page<CommentVO> page = (Page<CommentVO>) commentList;
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 补全评论的审核状态描述与评论类型描述
     * @param commentVO
     */
    private void fillDesc(CommentVO commentVO) {
        //补全审核状态描述
        Integer status = commentVO.getStatus();
        if (status != null) {
            switch (status) {
                case NumberConstant.COMMENT_STATUS_CHECKING:
                    commentVO.setStatusDesc(StringConstant.COMMENT_STATUS_CHECKING_DESC);
                    break;
                case NumberConstant.COMMENT_STATUS_APPROVED:
                    commentVO.setStatusDesc(StringConstant.COMMENT_STATUS_APPROVED_DESC);
                    break;
                case NumberConstant.COMMENT_STATUS_VIOLATION:
                    commentVO.setStatusDesc(StringConstant.COMMENT_STATUS_VIOLATION_DESC);
                    break;
                default:
                    break;
            }
        }

        //补全评论类型描述
        Integer commentType = commentVO.getCommentType();
        if (commentType != null) {
            switch (commentType) {
                case NumberConstant.COMMENT_TYPE_BAD:
                    commentVO.setCommentTypeDesc(StringConstant.COMMENT_TYPE_BAD_DESC);
                    break;
                case NumberConstant.COMMENT_TYPE_GOOD:
                    commentVO.setCommentTypeDesc(StringConstant.COMMENT_TYPE_GOOD_DESC);
                    break;
                default:
                    break;
            }
        }
    }
}
