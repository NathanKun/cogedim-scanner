package com.catprogrammer.cogedimscanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CogedimScannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CogedimScannerApplication.class, args);
    }

}
