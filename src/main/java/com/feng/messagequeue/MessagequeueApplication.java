package com.feng.messagequeue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "com.feng.messagequeue")
public class MessagequeueApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessagequeueApplication.class, args);
    }

}
