package com.itsean.swapu_admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 管理端启动类
 * <p>
 * 额外扫描 com.itsean.common，使公共模块里的 @Component（JwtProperties、AliOssProperties）能被注册为 Bean。
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.itsean.swapu_admin", "com.itsean.common"})
@EnableScheduling
public class SwapUAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwapUAdminApplication.class, args);
    }

}
