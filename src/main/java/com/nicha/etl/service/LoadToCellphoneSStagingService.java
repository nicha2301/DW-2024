package com.nicha.etl.service;

import com.nicha.etl.entity.config.DataSourceConfig;
import com.nicha.etl.entity.config.ProcessLogging;
import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.repository.config.DataSourceConfigRepository;
import com.nicha.etl.repository.config.ProcessTrackerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Service
public class LoadToCellphoneSStagingService extends AbstractEtlService {

    private final JdbcTemplate jdbcTemplate;
    private final DataSourceConfigRepository dataSourceConfigRepository;

    @Autowired
    protected LoadToCellphoneSStagingService(LoggingService loggingService,
                                             ProcessTrackerRepository trackerRepo,
                                             JdbcTemplate jdbcTemplate,
                                             DataSourceConfigRepository dataSourceConfigRepository) {
        super(loggingService, trackerRepo, "Import CellphoneS data to CellphoneS Staging");
        this.jdbcTemplate = jdbcTemplate;
        this.dataSourceConfigRepository = dataSourceConfigRepository;
    }

    @Override
    protected void process(boolean forcedRun) {
        logProcess(ProcessLogging.LogLevel.DEBUG, "Getting all data source config to load to staging.");
        List<DataSourceConfig> configs = this.dataSourceConfigRepository.findAll();

        for (DataSourceConfig dataSourceConfig : configs) {
            logProcess(ProcessLogging.LogLevel.DEBUG, "Working with data source: " + dataSourceConfig.getName());
            loadToStaging(dataSourceConfig);
            logProcess(ProcessLogging.LogLevel.DEBUG, "Completed load with data source: " + dataSourceConfig.getName());
        }
    }

    private void loadToStaging(DataSourceConfig dataSourceConfig) {
        String csvFileURL = dataSourceConfig.getCrawlSaveLocation();
        if (csvFileURL == null) {
            throw new RuntimeException("Crawl save location not found.");
        }

        ProcessTracker crawlTracker = tracker.getRequiredProcess();
        Calendar c1 = Calendar.getInstance();
        System.out.println(crawlTracker.getStartTime());
        c1.setTime(crawlTracker.getStartTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy_HHmm");
        csvFileURL = csvFileURL.replace("ddmmyy_hhmm", dateFormat.format(c1.getTime()));

        File csvFile = new File(csvFileURL);
        if (!csvFile.exists()) {
            throw new RuntimeException("Crawl save file not found, " + csvFile.getAbsolutePath());
        }
        loadIntoTable(csvFile.getAbsolutePath().replace(File.separatorChar, '/'));
    }

    private void loadIntoTable(String csvFileURL) {
        jdbcTemplate.update("DELETE FROM staging_head_phone");
        jdbcTemplate.execute("LOAD DATA INFILE '"+ csvFileURL + "' REPLACE INTO TABLE staging_head_phone " +
                "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\\n' " +
                "IGNORE 1 LINES " +
                "(product_id,name,brand,type,price,warranty_info,feature,voice_control,microphone,battery_life,dimensions,weight,compatibility) " +
                "SET created_at = NOW()");
        logProcess(ProcessLogging.LogLevel.DEBUG, "Loaded yay!");
    }
}
