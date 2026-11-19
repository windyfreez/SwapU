package com.itsean.campus_second_hand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 用户端启动类
 * <p>
 * knife4j / springfox 与 WebMvcConfigurationSupport 的兼容配置保持不变；
 * 额外扫描 com.itsean.common，使公共模块里的 @Component（JwtProperties、AliOssProperties）能被注册为 Bean。
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.itsean.campus_second_hand", "com.itsean.common"})
@EnableScheduling
public class CampusSecondHandApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusSecondHandApplication.class, args);
    }

}
