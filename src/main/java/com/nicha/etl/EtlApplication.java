package com.nicha.etl;

import com.nicha.etl.service.CrawlService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EtlApplication {

    public static void main(String[] args) {
        SpringApplication.run(EtlApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(CrawlService crawlService) {
        return args -> {
            // Crawl and save data
            crawlService.crawlDataAndSaveToStaging();
        };
    }

}
