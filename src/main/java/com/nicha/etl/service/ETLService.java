package com.nicha.etl.service;

import com.nicha.etl.repository.config.ProcessTrackerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ETLService extends AbstractEtlService {

    private final CrawlService crawlService;
    private final ImportToStagingService importToStagingService;
    private final CleanService cleanService;
    private final LoadService loadService;

    @Autowired
    protected ETLService(LoggingService loggingService,
                         ProcessTrackerRepository trackerRepo,
                         CrawlService crawlService,
                         ImportToStagingService importToStagingService,
                         CleanService cleanService,
                         LoadService loadService) {
        super(loggingService, trackerRepo, "Main");
        this.crawlService = crawlService;
        this.importToStagingService = importToStagingService;
        this.cleanService = cleanService;
        this.loadService = loadService;
    }

    @Override
    protected void process(boolean forceRun) {
        // 1. Crawl Data
        crawlService.run(forceRun);
        // 1.5 Load to staging
        importToStagingService.run(forceRun);
        // 2. Clean Data
        cleanService.run(forceRun);
        // 3. Load Data to Warehouse
        loadService.run(forceRun);
    }
}
