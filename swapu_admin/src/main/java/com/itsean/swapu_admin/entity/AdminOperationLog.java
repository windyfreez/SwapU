package com.itsean.swapu_admin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员操作日志实体，对应 admin_operation_log 表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminOperationLog {

    private Long id;//日志ID

    private Long adminId;//操作人ID

    private String adminUsername;//操作人账号（冗余快照，账号改名后仍可追溯）

    private String action;//动作：CREATE/UPDATE/STATUS/DELETE/RESET_PASSWORD

    private String targetType;//对象类型：ADMIN

    private Long targetId;//对象ID

    private String detail;//变更摘要，如 role:2->3

    private String ip;//操作IP

    private LocalDateTime createTime;//操作时间

}
