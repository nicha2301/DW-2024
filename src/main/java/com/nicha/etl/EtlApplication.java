package com.nicha.etl;

import com.nicha.etl.service.CrawlService;
import com.nicha.etl.service.ETLService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EtlApplication {

    private Logger logger = LoggerFactory.getLogger(EtlApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EtlApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(ETLService etlService) {
        return args -> {
            logger.info("EXECUTING : command line runner");
            // Crawl and save data
            etlService.runETLProcess(true);
        };
    }

}
