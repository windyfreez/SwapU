package com.itsean.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long id;

    private String studentId;

    private String username;

    private String nickname;

    /**
     * 登录密码（MD5 密文）。
     * 加 @JsonIgnore：GET /user/{id} 会把实体直接序列化返回，不加会把密码哈希泄露给任意登录用户
     */
    @JsonIgnore
    private String password;

    /**
     * 支付密码（BCrypt 密文），与登录密码完全独立。
     * 为 NULL 表示历史账号尚未初始化，服务端会在首次余额支付时惰性升级。
     * 加 @JsonIgnore 保证任何接口都不会把密文返回给前端
     */
    @JsonIgnore
    private String payPassword;

    private String avatar;//头像

    private String phone;

    private String email;

    private String college;

    private BigDecimal balance = BigDecimal.ZERO;

    private Integer creditScore;

    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}
