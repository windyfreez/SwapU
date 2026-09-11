package com.itsean.campus_second_hand.controller.user;

import com.itsean.campus_second_hand.service.CommentService;
import com.itsean.pojo.PageResult;
import com.itsean.pojo.Result;
import com.itsean.pojo.dto.CommentAddDTO;
import com.itsean.pojo.dto.CommentPageQueryDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
@Slf4j
@Api(tags = "评论接口")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 新增评论
     * @param commentAddDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("新增评论")
    public Result addComment(@RequestBody CommentAddDTO commentAddDTO) {
        log.info("新增评论：{}", commentAddDTO);
        commentService.addComment(commentAddDTO);
        return Result.success("评论成功，等待审核");
    }

    /**
     * 删除评论
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除评论")
    public Result deleteComment(@PathVariable Long id) {
        log.info("删除评论：{}", id);
        commentService.deleteComment(id);
        return Result.success("删除成功");
    }

    /**
     * 分页查询当前用户发出的所有评论
     * @param commentPageQueryDTO
     * @return
     */
    @GetMapping("/my")
    @ApiOperation("分页查询当前用户发出的所有评论")
    public Result<PageResult> myComments(CommentPageQueryDTO commentPageQueryDTO) {
        log.info("分页查询当前用户发出的所有评论：{}", commentPageQueryDTO);
        PageResult pageResult = commentService.pageQueryMyComments(commentPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 分页查询某个用户收到的所有评论
     * @param receiverId
     * @param commentPageQueryDTO
     * @return
     */
    @GetMapping("/received/{receiverId}")
    @ApiOperation("分页查询某个id用户收到的所有评论")
    public Result<PageResult> receivedComments(@PathVariable Long receiverId, CommentPageQueryDTO commentPageQueryDTO) {
        log.info("分页查询用户{}收到的所有评论：{}", receiverId, commentPageQueryDTO);
        PageResult pageResult = commentService.pageQueryReceivedComments(receiverId, commentPageQueryDTO);
        return Result.success(pageResult);
    }
}
