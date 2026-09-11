package com.itsean.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 用户评论实体，对应comment表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    private Long id;

    private Long senderId;//评论发送者ID

    private Long receiverId;//评论接收者ID

    private String image;//评论插入图片（仅支持插入一张）

    private String content;//评论文字内容

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime sendTime;//评论发送时间

    private Integer status;//审核状态：0审核中，1过审，2违禁

    private Integer commentType;//评论类型：0差评，1好评

}
