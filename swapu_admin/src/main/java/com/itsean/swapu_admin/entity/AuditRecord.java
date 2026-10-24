package com.itsean.swapu_admin.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 审核流水实体，对应 audit_record 表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditRecord {

    private Long id;//流水ID

    private Integer type;//审核类型：1商品 2评论 3头像

    private Long targetId;//被审对象ID（商品/评论/用户）

    private Integer result;//审核结论：1通过 2驳回

    private String reason;//驳回原因

    private String aiResult;//AI审核结论（JSON，AI审核接入后）

    private Long reviewerId;//审核人ID

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime reviewTime;//审核时间

}
