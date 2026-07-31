package com.itsean.campus_second_hand.interceptor;

import com.itsean.campus_second_hand.constant.JwtClaimsConstant;
import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.campus_second_hand.properties.JwtProperties;
import com.itsean.campus_second_hand.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        log.info("当前线程的ID:"+Thread.currentThread().getId());

        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());

        //2、判断令牌是否存在，如果不存在，直接返回401
        if (token == null || token.isEmpty()) {
            log.warn("请求头中未携带token，路径:{}", request.getRequestURI());
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\",\"data\":null}");
            return false;
        }

        //3、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户id：{}", userId);
            BaseContext.setCurrentId(userId);
            //4、通过，放行
            return true;
        } catch (Exception ex) {
            //5、不通过，响应401状态码和JSON格式的错误信息
            log.error("JWT解析失败:{}", ex.getMessage());
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\",\"data\":null}");
            return false;
        }
    }
}