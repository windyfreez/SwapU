package com.itsean.swapu_admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审核流水数据
 */
@Data
@ApiModel(description = "审核流水数据")
public class AuditRecordVO implements Serializable {

    @ApiModelProperty("流水ID")
    private Long id;

    @ApiModelProperty("审核类型：1商品 2评论 3头像")
    private Integer type;

    @ApiModelProperty("被审对象ID（商品/评论/用户）")
    private Long targetId;

    @ApiModelProperty("审核结论：1通过 2驳回")
    private Integer result;

    @ApiModelProperty("驳回原因")
    private String reason;

    @ApiModelProperty("AI审核结论（JSON，AI审核接入后）")
    private String aiResult;

    @ApiModelProperty("审核人ID")
    private Long reviewerId;

    @ApiModelProperty("审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime reviewTime;

}
