package com.nicha.etl;

import com.nicha.etl.service.ETLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
public class EtlApplication {

    private final Logger logger = LoggerFactory.getLogger(EtlApplication.class);

    public static void main(String[] args) throws IOException {
        SpringApplication.run(EtlApplication.class, args);
    }

}
