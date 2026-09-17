package com.itsean.swapu_admin.config;

import com.itsean.swapu_admin.interceptor.JwtTokenAdminInterceptor;
import com.itsean.swapu_admin.interceptor.JwtTokenUserInterceptor;
import com.itsean.swapu_admin.properties.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    /**
     * 无需令牌即可访问的路径：文档静态资源与错误页
     * 注意：/error 必须放行，否则 404/500 转发到错误页时会被令牌拦截器改写成“未登录”，掩盖真实错误
     */
    private static final String[] PUBLIC_PATH_PATTERNS = {
            "/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs/**",
            "/swagger-ui/**", "/swagger-ui.html/**", "/favicon.ico", "/error"
    };

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;
    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 通过knife4j生成管理端接口文档
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
                //全局token请求头：文档页调试受保护接口时填入登录返回的令牌即可
                .globalRequestParameters(buildTokenParameter(jwtProperties.getAdminTokenName(),
                        "管理端jwt令牌：调用 /admin/login 获取后填入"))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.itsean.swapu_admin.controller.admin"))
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
                .globalRequestParameters(buildTokenParameter(jwtProperties.getUserTokenName(),
                        "用户端jwt令牌：调用 /user/login 获取后填入"))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.itsean.swapu_admin.controller.user"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    /**
     * 构建全局令牌请求头参数
     *
     * @param tokenName   请求头名称，取自 jwt 配置的 token-name
     * @param description 参数说明
     * @return 全局请求参数集合
     */
    private List<RequestParameter> buildTokenParameter(String tokenName, String description) {
        List<RequestParameter> requestParameters = new ArrayList<>();
        requestParameters.add(new RequestParameterBuilder()
                .name(tokenName)
                .description(description)
                .in(ParameterType.HEADER)
                //非必填：登录、公开接口无需携带令牌
                .required(false)
                .build());
        return requestParameters;
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

    /**
     * 注册自定义拦截器
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        //管理端接口：使用 admin-secret-key 校验管理端令牌，登录接口放行
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");

        //用户端接口：使用 user-secret-key 校验用户端令牌
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/**")
                .excludePathPatterns("/user/login", "/user/register")
                .excludePathPatterns("/product/detail/**")
                .excludePathPatterns("/category/list")
                .excludePathPatterns("/product/hot")
                .excludePathPatterns(PUBLIC_PATH_PATTERNS);

    }


}
