package com.maplestone.dataCollect;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.maplestone.**")
@OpenAPIDefinition(servers = { @Server(url = "/", description = "Default Server URL") })
@EnableAsync
@EnableScheduling
public class DataCollectApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataCollectApplication.class, args);
    }

}
