package com.itsean.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 设置/重置支付密码请求参数
 * <p>
 * 用登录密码证明账号归属，然后把支付密码以 BCrypt 密文落库。
 * 支付密码与登录密码彼此独立：改登录密码不会同步改支付密码（除非两者仍是同一个值）。
 */
@Data
public class PayPasswordDTO {

    /** 登录密码，用于校验当前操作人是账号本人 */
    @NotBlank(message = "登录密码不能为空")
    private String loginPassword;

    /** 新的支付密码 */
    @NotBlank(message = "支付密码不能为空")
    @Size(min = 6, max = 20, message = "支付密码长度需为6-20位")
    private String payPassword;
}
