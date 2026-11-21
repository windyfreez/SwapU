package com.itsean.swapu_admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 管理员操作日志分页查询条件
 * <p>
 * 说明：管理端模块目前未引入 Bean Validation 实现（pom 中没有 spring-boot-starter-validation），
 * 因此这里不加校验注解，分页参数由 Service 层归一化并限制上限，与审核流水查询保持一致。
 */
@Data
@ApiModel(description = "管理员操作日志分页查询条件")
public class AdminOperationLogPageQueryDTO {

    @ApiModelProperty("页码，缺省 1")
    private Integer pageNum;

    @ApiModelProperty("每页条数，缺省 10，上限 100")
    private Integer pageSize;

    @ApiModelProperty("动作：CREATE/UPDATE/STATUS/DELETE/RESET_PASSWORD")
    private String action;

    @ApiModelProperty("操作人账号（模糊匹配）")
    private String adminUsername;

    @ApiModelProperty("开始时间（ISO，如 2026-11-21T10:00）")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间（ISO，如 2026-11-21T18:00）")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

}
