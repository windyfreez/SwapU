package com.itsean.campus_second_hand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CampusSecondHandApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusSecondHandApplication.class, args);
    }

}
