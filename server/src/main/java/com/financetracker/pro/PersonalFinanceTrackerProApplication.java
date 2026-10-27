package com.financetracker.pro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PersonalFinanceTrackerProApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalFinanceTrackerProApplication.class, args);
    }
}
