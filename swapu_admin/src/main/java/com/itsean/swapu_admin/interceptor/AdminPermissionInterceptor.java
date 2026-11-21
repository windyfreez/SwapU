package com.itsean.swapu_admin.interceptor;

import com.itsean.common.constant.MessageConstant;
import com.itsean.common.context.BaseContext;
import com.itsean.swapu_admin.annotation.RequirePermission;
import com.itsean.swapu_admin.constant.AdminPermission;
import com.itsean.swapu_admin.constant.ErrorCodeConstant;
import com.itsean.swapu_admin.exception.AdminException;
import com.itsean.swapu_admin.service.AdminPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * 管理端接口权限拦截器
 * <p>
 * 放在 {@link JwtTokenAdminInterceptor} 之后执行：
 * 令牌校验只保证「你是谁」，这里保证「你能做什么」。
 * 只处理带 {@link RequirePermission} 的接口，未标注的接口（登录、改自己密码、查自己信息）直接放行。
 */
@Component
@Slf4j
public class AdminPermissionInterceptor implements HandlerInterceptor {

    /**
     * 同一请求内缓存已解析的权限集合，避免一个请求多次查库
     */
    private static final String CACHED_PERMISSIONS_ATTR = "admin.permissions";

    @Autowired
    private AdminPermissionService adminPermissionService;

    /**
     * 进入 Controller 前校验权限
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return true 放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 静态资源等非动态方法直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequirePermission required = resolveRequirement(handlerMethod);

        // 没标注权限点的接口，登录即可访问
        if (required == null) {
            return true;
        }

        Set<AdminPermission> permissions = currentPermissions(request);
        if (!permissions.contains(required.value())) {
            log.warn("接口权限不足，adminId：{}，uri：{}，所需权限：{}",
                    BaseContext.getCurrentId(), request.getRequestURI(), required.value());
            throw new AdminException(MessageConstant.ADMIN_PERMISSION_DENIED, ErrorCodeConstant.FORBIDDEN);
        }
        return true;
    }

    /**
     * 在同一请求内复用已解析的权限集合
     *
     * @param request 请求
     * @return 权限集合
     */
    @SuppressWarnings("unchecked")
    private Set<AdminPermission> currentPermissions(HttpServletRequest request) {
        Object cached = request.getAttribute(CACHED_PERMISSIONS_ATTR);
        if (cached instanceof Set) {
            return (Set<AdminPermission>) cached;
        }
        Set<AdminPermission> permissions = adminPermissionService.currentPermissions();
        request.setAttribute(CACHED_PERMISSIONS_ATTR, permissions);
        return permissions;
    }

    /**
     * 解析接口所需权限：方法上的注解优先，其次取类上的注解
     * <p>
     * 用 ClassUtils.getUserClass 取原始类，避免 Controller 被 AOP 代理后类级注解读不到。
     *
     * @param handlerMethod 处理器方法
     * @return 权限声明，未声明返回 null
     */
    private RequirePermission resolveRequirement(HandlerMethod handlerMethod) {
        RequirePermission onMethod = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (onMethod != null) {
            return onMethod;
        }
        Class<?> userClass = ClassUtils.getUserClass(handlerMethod.getBeanType());
        return userClass.getAnnotation(RequirePermission.class);
    }
}
