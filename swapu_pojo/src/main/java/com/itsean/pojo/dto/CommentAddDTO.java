package com.itsean.pojo.dto;

import lombok.Data;

/**
 * 新增评论请求参数
 */
@Data
public class CommentAddDTO {

    private Long receiverId;//评论接收者ID

    private String image;//评论插入图片（仅支持插入一张）

    private String content;//评论文字内容

    private Integer commentType;//评论类型：0差评，1好评，不传默认好评

}
