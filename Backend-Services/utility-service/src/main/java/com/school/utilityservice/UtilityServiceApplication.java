package com.school.utilityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"com.school.utilityservice", "com.school.common", "com.pawan.share.jwt"})
public class UtilityServiceApplication {
    public static void main(String[] args) { SpringApplication.run(UtilityServiceApplication.class, args); }
}
