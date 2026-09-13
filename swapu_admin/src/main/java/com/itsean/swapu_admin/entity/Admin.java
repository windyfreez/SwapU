package com.itsean.swapu_admin.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 管理员实体，对应 admin 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    private Long id;//管理员ID

    private String username;//登录账号

    @JsonIgnore
    private String password;//登录密码（BCrypt 密文），禁止序列化返回给前端

    private String name;//姓名

    private String avatar;//头像地址

    private Integer role;//角色：1超级管理员 2普通管理员

    private Integer status;//状态：1启用 0禁用

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;//创建时间

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;//更新时间

}
