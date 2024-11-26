package com.nicha.etl.service;

import com.nicha.etl.entity.config.DataSourceConfig;
import com.nicha.etl.entity.config.ProcessLogging;
import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.repository.config.DataSourceConfigRepository;
import com.nicha.etl.repository.config.ProcessTrackerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

@Service
public class CrawlService {

    private final LoggingService loggingService;
    private final ProcessTrackerRepository processTrackerRepository;
    private final DataSourceConfigRepository dataSourceConfigRepository;
    private ProcessTracker currentProcessTracker;

    @Autowired
    public CrawlService(LoggingService loggingService, ProcessTrackerRepository processTrackerRepository, DataSourceConfigRepository dataSourceConfigRepository) {
        this.loggingService = loggingService;
        this.dataSourceConfigRepository = dataSourceConfigRepository;
        this.processTrackerRepository = processTrackerRepository;

        this.currentProcessTracker = this.processTrackerRepository.findByProcessName(getClass().getName());
        if (this.currentProcessTracker == null) {
            this.currentProcessTracker = new ProcessTracker();
            this.currentProcessTracker.setProcessName(getClass().getName());
            this.currentProcessTracker.setStatus(ProcessTracker.ProcessStatus.C_RE);
            this.processTrackerRepository.save(this.currentProcessTracker);
        }
    }

    private boolean sameDate(Timestamp timestamp1, Timestamp timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        if (timestamp1 == null || timestamp2 == null)
            return timestamp1 == timestamp2;
        cal1.setTime(timestamp1);
        cal2.setTime(timestamp2);
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

    public void crawlDataSourcesAndSaveToStaging() {
        if (this.currentProcessTracker.getStatus() == ProcessTracker.ProcessStatus.C_E) {
            loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.ERROR, "The crawling process is running at the moment.");
            return;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (sameDate(now, this.currentProcessTracker.getStartTime()) && this.currentProcessTracker.getStatus() == ProcessTracker.ProcessStatus.C_SE) {
            loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.ERROR, "The crawling has been done for the day.");
            return;
        }

        this.currentProcessTracker.setStatus(ProcessTracker.ProcessStatus.C_E);
        this.currentProcessTracker.setStartTime(new Timestamp(System.currentTimeMillis()));
        this.processTrackerRepository.save(this.currentProcessTracker);

        loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.INFO, "Getting all data source config to crawl.");
        List<DataSourceConfig> configs = this.dataSourceConfigRepository.findAll();

        // Iterate and work on them
        for (DataSourceConfig config: configs) {
            loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.INFO, "DataSource: " + config.toString());
        }

        this.currentProcessTracker.setStatus(ProcessTracker.ProcessStatus.C_SE);
        this.currentProcessTracker.setEndTime(new Timestamp(System.currentTimeMillis()));
        this.processTrackerRepository.save(this.currentProcessTracker);
    }

}
