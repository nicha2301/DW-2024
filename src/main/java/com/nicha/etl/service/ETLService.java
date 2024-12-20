package com.nicha.etl.service;

import com.nicha.etl.entity.config.ProcessLogging;
import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.repository.config.ProcessTrackerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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

    private void processFailed() {
        ProcessTracker tracker1 = trackerRepo.findByProcessName(crawlCellphoneSService.getProcessName());
        if (tracker1.getStatus() == ProcessTracker.ProcessStatus.FAILED) {
            // 1. Crawl Data
            crawlCellphoneSService.run(true);
        }
        tracker1 = trackerRepo.findByProcessName(loadToCellphoneSStagingService.getProcessName());
        if (tracker1.getStatus() == ProcessTracker.ProcessStatus.FAILED) {
            // 1.5 Load to staging
            loadToCellphoneSStagingService.run(true);
        }
        tracker1 = trackerRepo.findByProcessName(transformCellphoneSAndLoadToStagingService.getProcessName());
        if (tracker1.getStatus() == ProcessTracker.ProcessStatus.FAILED) {
            // 2. Clean Data
            transformCellphoneSAndLoadToStagingService.run(true);
        }
        tracker1 = trackerRepo.findByProcessName(crawlCellphoneSService.getProcessName());
        if (tracker1.getStatus() == ProcessTracker.ProcessStatus.FAILED) {
            // 3. Load Data to Warehouse
            loadToWarehouseService.run(true);
        }
    }

    public void runFailedProcessesOnly() {
        boolean forceRun = false;
        SimpleDateFormat format = new SimpleDateFormat("mm:ss.SSS");
        Timestamp start = new Timestamp(System.currentTimeMillis());
        try {
            start = preRunCheck(forceRun);
            processFailed();
            // Set state to SUCCESS and log end because it was finished without exception
            postRunMethod(format, start);
        }
        catch (Exception e) {
            postRunMethodExceptionCatch(e, start);
            throw new RuntimeException(e);
        }
    }

    public void printLogMessage(int amount) {
        List<ProcessLogging> result = loggingService.getLogMessages(amount);

        String formatString = "%s:%s - %s (%s) [%s, %s]\n\"%s\"\n";
        System.out.printf(formatString, "LogID", "Process ID", "Log Date", "Log Level", "Log Process Start Time", "Log Process End Time", "Log Message");
        for (ProcessLogging log : result) {
            System.out.printf(formatString, log.getId(), "", log.getDate(), log.getLevel(), log.getProcessStart(), log.getProcessEnd(), log.getMessage());
        }
    }
}
