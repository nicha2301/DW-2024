package com.nicha.etl.service;

import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.repository.config.ProcessTrackerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Service;

import java.util.List;

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
    protected void process(@Option(longNames = "--force-run") boolean forceRun) {
        // 1. Crawl Data
        crawlCellphoneSService.run(forceRun);
        // 1.5 Load to staging
        loadToCellphoneSStagingService.run(forceRun);
        // 2. Clean Data
        transformCellphoneSAndLoadToStagingService.run(forceRun);
        // 3. Load Data to Warehouse
        loadToWarehouseService.run(forceRun);
    }

    public void printAllProcessTrackerStatus() {
        List<ProcessTracker> result = trackerRepo.findAll();
        String formatString = "%-3s\t%-12s\t%-60s\t%-20s\t%-24s\t%-24s\n";
        System.out.printf(formatString, "PID", "Required PID", "Process Name", "Process Status", "Process Last Start Time", "Process Last End Time");
        for (ProcessTracker tracker : result) {
            ProcessTracker required = tracker.getRequiredProcess();
            System.out.printf(formatString, tracker.getId(), required == null ? "None" : required.getId(), tracker.getProcessName(), tracker.getStatus(), tracker.getStartTime(), tracker.getEndTime());
        }
    }

    public void runCrawlCellphoneSService(boolean forceRun) {
        crawlCellphoneSService.run(forceRun);
    }

    public void runLoadToCellphoneSStagingService(boolean forceRun) {
        loadToCellphoneSStagingService.run(forceRun);
    }

    public void runTransformCellphoneSAndLoadToStagingService(boolean forceRun) {
        transformCellphoneSAndLoadToStagingService.run(forceRun);
    }

    public void loadToWarehouseService(boolean forceRun) {
        loadToCellphoneSStagingService.run(forceRun);
    }
}
