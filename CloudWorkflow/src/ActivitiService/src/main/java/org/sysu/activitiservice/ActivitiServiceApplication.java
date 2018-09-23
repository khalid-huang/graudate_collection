package org.sysu.activitiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ActivitiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivitiServiceApplication.class, args);
    }
}
