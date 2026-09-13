package com.itsean.swapu_admin.interceptor;

import com.itsean.swapu_admin.constant.JwtClaimsConstant;
import com.itsean.swapu_admin.context.BaseContext;
import com.itsean.swapu_admin.properties.JwtProperties;
import com.itsean.swapu_admin.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 管理端 jwt 令牌校验的拦截器
 * <p>
 * 使用 admin-secret-key 校验管理端令牌，与用户端令牌相互隔离。
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验管理端jwt
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return true 放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //判断当前拦截到的是Controller的方法还是其他资源，非动态方法直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        //1、从请求头中获取管理端令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        //2、判断令牌是否存在，如果不存在，直接返回401
        if (token == null || token.isEmpty()) {
            log.warn("管理端请求头中未携带token，路径:{}", request.getRequestURI());
            writeUnauthorized(response);
            return false;
        }

        //3、校验令牌
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long adminId = Long.valueOf(claims.get(JwtClaimsConstant.ADMIN_ID).toString());
            log.info("当前管理员id：{}", adminId);
            BaseContext.setCurrentId(adminId);
            //4、通过，放行
            return true;
        } catch (Exception ex) {
            //5、不通过，响应401状态码和JSON格式的错误信息
            log.error("管理端JWT解析失败:{}", ex.getMessage());
            writeUnauthorized(response);
            return false;
        }
    }

    /**
     * 请求处理完成后清理 ThreadLocal，避免线程复用导致身份串号
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @param ex       处理过程中的异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }

    /**
     * 输出未登录的统一JSON响应
     *
     * @param response 响应
     */
    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\",\"data\":null}");
    }

}
