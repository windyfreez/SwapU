package com.itsean.swapu_admin.config;

import com.itsean.common.properties.JwtProperties;
import com.itsean.swapu_admin.interceptor.AdminPermissionInterceptor;
import com.itsean.swapu_admin.interceptor.JwtTokenAdminInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置类，注册web层相关组件
 * <p>
 * 管理端只对外暴露 /admin/** 一组接口，因此只保留管理端令牌拦截器与一个接口文档分组，
 * 用户端接口与其令牌拦截器属于用户端应用，不再在此重复注册。
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;
    @Autowired
    private AdminPermissionInterceptor adminPermissionInterceptor;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 通过knife4j生成管理端接口文档
     * @return 文档分组
     */
    @Bean
    public Docket docket1() {
        log.info("准备生成管理端接口文档...");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("SwapU云市集项目接口文档")
                .version("1.0")
                .description("SwapU云市集管理端接口文档")
                .build();
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("管理端接口")
                .apiInfo(apiInfo)
                //全局token请求头：文档页调试受保护接口时填入登录返回的令牌即可
                .globalRequestParameters(buildTokenParameter(jwtProperties.getAdminTokenName(),
                        "管理端jwt令牌：调用 /admin/login 获取后填入"))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.itsean.swapu_admin.controller.admin"))
                .paths(PathSelectors.any())
                .build();
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
     * @param registry 资源处理器注册表
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //本类继承 WebMvcConfigurationSupport，会绕过 Spring Boot 对消息转换器的定制，
        //默认的 StringHttpMessageConverter 用 ISO-8859-1 编码，会把中文写成 '?'
        converters.add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        super.extendMessageConverters(converters);
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始设置静态资源映射...");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 注册自定义拦截器：只保护 /admin/**，登录接口放行
     * @param registry 拦截器注册表
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        // 第一道：管理端接口使用 admin-secret-key 校验「你是谁」，登录接口放行
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login")
                  //验证码接口在登录页就要调用，令牌与权限两道关卡都必须放行
                  .excludePathPatterns("/admin/captcha/**");

        // 第二道：「你能做什么」，只拦截标注了 @RequirePermission 的接口
        // 注册顺序即执行顺序，必须排在令牌校验之后，否则取不到 BaseContext 中的 adminId
        registry.addInterceptor(adminPermissionInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login")
                  //验证码接口在登录页就要调用，令牌与权限两道关卡都必须放行
                  .excludePathPatterns("/admin/captcha/**");
    }

}
