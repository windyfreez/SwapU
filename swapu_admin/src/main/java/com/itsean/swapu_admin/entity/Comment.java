package com.itsean.swapu_admin.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 评论实体，对应 comment 表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    private Long id;//评论ID

    private Long senderId;//评论人ID

    private Long receiverId;//评论接收人ID

    private String image;//评论图片（单张）

    private String content;//评论内容

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime sendTime;//评论时间

    private Integer status;//审核状态：0待审核 1已发布 2审核驳回

    private Integer commentType;//评论类型：0差评 1好评

}
