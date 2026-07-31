package com.itsean.swapu_admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SwapUAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwapUAdminApplication.class, args);
    }

}
