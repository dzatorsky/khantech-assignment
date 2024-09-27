package com.khantech.assignment;

import org.springframework.boot.SpringApplication;

public class TestKhantechApplication {

    public static void main(String[] args) {
        SpringApplication.from(KhantechApplication::main).with(TestContainerConfiguration.class).run(args);
    }

}
