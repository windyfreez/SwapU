package com.itsean.swapu_admin.annotation;

import com.itsean.swapu_admin.constant.AdminPermission;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明接口所需的权限点，由 {@code AdminPermissionInterceptor} 在进入 Controller 前校验
 * <p>
 * 用法：可以标在方法上（只对该方法生效），也可以标在类上（对该 Controller 全部方法生效）；
 * 两者同时存在时以方法上的为准。没有该注解的接口表示「登录即可访问」（如登录、改自己密码）。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 访问该接口所需的权限点
     *
     * @return 权限点
     */
    AdminPermission value();
}
