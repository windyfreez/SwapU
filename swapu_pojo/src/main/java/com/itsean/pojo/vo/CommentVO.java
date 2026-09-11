package com.itsean.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 评论展示VO，同时冗余发送者与接收者的基础信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentVO {

    private Long id;

    private Long senderId;

    private String senderName;//评论发送者用户名

    private String senderAvatar;//评论发送者头像

    private Long receiverId;

    private String receiverName;//评论接收者用户名

    private String receiverAvatar;//评论接收者头像

    private String image;//评论插入图片（仅支持插入一张）

    private String content;//评论文字内容

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime sendTime;//评论发送时间

    private Integer status;//审核状态：0审核中，1过审，2违禁

    private String statusDesc;//审核状态描述：审核中/过审/违禁

    private Integer commentType;//评论类型：0差评，1好评

    private String commentTypeDesc;//评论类型描述：差评/好评

}
