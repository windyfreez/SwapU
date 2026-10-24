package com.itsean.swapu_admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论审核列表数据
 */
@Data
@ApiModel(description = "评论审核列表数据")
public class CommentAuditVO implements Serializable {

    @ApiModelProperty("评论ID")
    private Long id;

    @ApiModelProperty("被评论人ID（receiver_id）")
    private Long receiverId;

    @ApiModelProperty("被评论人账号")
    private String receiverUsername;

    @ApiModelProperty("被评论人昵称")
    private String receiverNickname;

    @ApiModelProperty("评论人ID（sender_id）")
    private Long userId;

    @ApiModelProperty("评论人账号")
    private String username;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("评论图片")
    private List<String> images;

    @ApiModelProperty("审核状态：0待审核 1已发布 2已驳回")
    private Integer status;

    @ApiModelProperty("评论时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}
