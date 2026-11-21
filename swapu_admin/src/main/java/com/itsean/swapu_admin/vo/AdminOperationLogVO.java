package com.itsean.swapu_admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员操作日志列表项
 */
@Data
@ApiModel(description = "管理员操作日志")
public class AdminOperationLogVO {

    @ApiModelProperty("日志ID")
    private Long id;

    @ApiModelProperty("操作人ID")
    private Long adminId;

    @ApiModelProperty("操作人账号")
    private String adminUsername;

    @ApiModelProperty("动作：CREATE/UPDATE/STATUS/DELETE/RESET_PASSWORD")
    private String action;

    @ApiModelProperty("动作中文描述")
    private String actionDesc;

    @ApiModelProperty("对象类型")
    private String targetType;

    @ApiModelProperty("对象ID")
    private Long targetId;

    @ApiModelProperty("变更摘要")
    private String detail;

    @ApiModelProperty("操作IP")
    private String ip;

    @ApiModelProperty("操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}
