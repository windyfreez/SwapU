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
 * 该拦截器拦截所有方法，向ThreadLocal注入上下文
 */
@Component
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProperties;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 非 Controller 方法（静态资源等）直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String token = request.getHeader(jwtProperties.getUserTokenName());
        if (token == null || token.isEmpty()) {
            // 游客：不设置 userId，放行
            return true;
        }
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            BaseContext.setCurrentId(userId);
        } catch (Exception ex) {
            log.warn("token解析失败，按游客处理，路径:{}", request.getRequestURI());
        }
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}