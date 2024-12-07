package com.nicha.etl.service;

import com.nicha.etl.repository.config.ProcessTrackerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ETLService extends AbstractEtlService {

    private final CrawlCellphoneSService crawlCellphoneSService;
    private final LoadToCellphoneSStagingService loadToCellphoneSStagingService;
    private final TransformCellphoneSAndLoadToStagingService transformCellphoneSAndLoadToStagingService;
    private final LoadToWarehouseService loadToWarehouseService;

    @Autowired
    protected ETLService(LoggingService loggingService,
                         ProcessTrackerRepository trackerRepo,
                         CrawlCellphoneSService crawlCellphoneSService,
                         LoadToCellphoneSStagingService loadToCellphoneSStagingService,
                         TransformCellphoneSAndLoadToStagingService transformCellphoneSAndLoadToStagingService,
                         LoadToWarehouseService loadToWarehouseService) {
        super(loggingService, trackerRepo, "Main");
        this.crawlCellphoneSService = crawlCellphoneSService;
        this.loadToCellphoneSStagingService = loadToCellphoneSStagingService;
        this.transformCellphoneSAndLoadToStagingService = transformCellphoneSAndLoadToStagingService;
        this.loadToWarehouseService = loadToWarehouseService;
    }

    @Override
    protected void process(boolean forceRun) {
        // 1. Crawl Data
        crawlCellphoneSService.run(forceRun);
        // 1.5 Load to staging
        loadToCellphoneSStagingService.run(forceRun);
        // 2. Clean Data
        transformCellphoneSAndLoadToStagingService.run(forceRun);
        // 3. Load Data to Warehouse
        loadToWarehouseService.run(forceRun);
    }
}
