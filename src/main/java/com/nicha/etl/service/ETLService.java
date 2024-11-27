package com.nicha.etl.service;

import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class ETLService {

    private final CrawlService crawlService;
    private final CleanService cleanService;
    private final LoadService loadService;
    private final LoggingService loggingService;

    public ETLService(CrawlService crawlService,
                      CleanService cleanService,
                      LoadService loadService,
                      LoggingService loggingService) {
        this.crawlService = crawlService;
        this.cleanService = cleanService;
        this.loadService = loadService;
        this.loggingService = loggingService;
    }

    public void runETLProcess() {
        Timestamp start = new Timestamp(System.currentTimeMillis());
        loggingService.logProcess(null, null, "ETL process begins", start, start);
        try {
            // Bắt đầu quy trình ETL
            // 1. Crawl Data
            crawlService.crawlDataSourcesAndSaveToStaging();

            // 2. Clean Data
            cleanService.cleanData();

            // 3. Load Data to Warehouse
            loadService.loadDataToWarehouse();

            // Kết thúc quá trình ETL
            loggingService.logProcess(null, null, "ETL process ended successfully", start, new Timestamp(System.currentTimeMillis()));

        } catch (Exception e) {
            loggingService.logProcess(null, null, "ETL process ended with error ".concat(e.getMessage()), start, new Timestamp(System.currentTimeMillis()));
        }
    }
}
