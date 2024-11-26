package com.nicha.etl.service;

import org.springframework.stereotype.Service;

@Service
public class ETLService {

    private final CrawlService crawlService;
    private final CleanService cleanService;
    private final LoggingService loggingService;

    public ETLService(CrawlService crawlService, CleanService cleanService, LoggingService loggingService) {
        this.crawlService = crawlService;
        this.cleanService = cleanService;
        this.loggingService = loggingService;
    }

    public void runETLProcess() {
        try {
            // Bắt đầu quy trình ETL
            // 1. Crawl Data
            crawlService.crawlDataSourcesAndSaveToStaging();

            // 2. Clean Data
            cleanService.cleanData();

            // Kết thúc quá trình ETL
//            loggingService.logProcess("ETL Process", "ETL process completed successfully", "SUCCESS");

        } catch (Exception e) {
//            loggingService.logProcess("ETL Process", "ETL process failed: " + e.getMessage(), "ERROR");
        }
    }
}
