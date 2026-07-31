package com.itsean.campus_second_hand.config;

import com.itsean.campus_second_hand.constant.JwtClaimsConstant;
import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.campus_second_hand.interceptor.JwtTokenUserInterceptor;
import com.itsean.campus_second_hand.properties.JwtProperties;
import com.itsean.campus_second_hand.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 通过knife4j生成接口文档
     * @return
     */
    @Bean
    public Docket docket1() {
        log.info("准备生成管理端接口文档...");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("SwapU云市集项目接口文档")
                .version("1.0")
                .description("SwapU云市集管理端接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("管理端接口")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.itsean.campus_second_hand.controller.admin"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }


    @Bean
    public Docket docket2() {
        log.info("准备生成用户端接口文档...");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("SwapU云市集项目接口文档")
                .version("1.0")
                .description("SwapU云市集用户端接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("用户端接口")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.itsean.campus_second_hand.controller.user"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    /**
     * 设置静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始设置静态资源映射...");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        log.info("当前线程的ID:"+Thread.currentThread().getId());

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getUserTokenName());

        if (token == null || token.isEmpty()) {
            log.warn("请求头中未携带token，路径:{}", request.getRequestURI());
            response.setStatus(401);
            return false;
        }

        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户id：{}", userId);
            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception ex) {
            log.error("JWT解析失败:{}", ex.getMessage());
            response.setStatus(401);
            return false;
        }
    }


    /**
     * 注册自定义拦截器
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login", "/user/register")
                .excludePathPatterns("/product/detail/**")
                .excludePathPatterns("/category/list")
                .excludePathPatterns("/product/hot")
                .excludePathPatterns("/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs/**", "/swagger-ui.html/**");

    }


}
