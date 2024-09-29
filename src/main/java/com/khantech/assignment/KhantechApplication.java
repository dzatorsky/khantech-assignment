package com.khantech.assignment;

import com.khantech.assignment.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class KhantechApplication {

    public static void main(String[] args) {
        SpringApplication.run(KhantechApplication.class, args);
    }

}
