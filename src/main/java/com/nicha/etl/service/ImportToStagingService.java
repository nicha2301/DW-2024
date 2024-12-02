package com.nicha.etl.service;

import com.nicha.etl.entity.config.DataSourceConfig;
import com.nicha.etl.entity.config.ProcessLogging;
import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.repository.config.DataSourceConfigRepository;
import com.nicha.etl.repository.config.ProcessTrackerRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Service
public class ImportToStagingService extends AbstractEtlService {

    private final JdbcTemplate jdbcTemplate;
    private final DataSourceConfigRepository dataSourceConfigRepository;

    protected ImportToStagingService(LoggingService loggingService,
                                     ProcessTrackerRepository trackerRepo,
                                     JdbcTemplate jdbcTemplate,
                                     DataSourceConfigRepository dataSourceConfigRepository) {
        super(loggingService, trackerRepo, "Import to Staging");
        this.jdbcTemplate = jdbcTemplate;
        this.dataSourceConfigRepository = dataSourceConfigRepository;

        ProcessTracker pt = this.trackerRepo.findByProcessName("Crawl Data");
        this.tracker.setRequiredProcess(pt);
        this.trackerRepo.save(this.tracker);
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

        ProcessTracker crawlTracker = trackerRepo.findByProcessName("Crawl Data");
        Calendar c1 =Calendar.getInstance();
        c1.setTime(crawlTracker.getStartTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy_hhmm");
        csvFileURL = csvFileURL.replace("ddmmyy_hhmm", dateFormat.format(c1.getTime()));
        File csvFile = new File(csvFileURL);
        if (!csvFile.exists()) {
            throw new RuntimeException("Crawl save file not found.");
        }
        String stagingTableName = dataSourceConfig.getStagingTableName();
        if (stagingTableName == null || stagingTableName.trim().isEmpty()) {
            throw new RuntimeException("Staging table name not found.");
        }
        String stagingTableFields = dataSourceConfig.getStagingFields();
        if (stagingTableFields == null || stagingTableFields.trim().isEmpty()) {
            throw new RuntimeException("Staging table fields not found.");
        }
        createTable(stagingTableName, stagingTableFields);
        loadIntoTable(stagingTableName, stagingTableFields, csvFile.getAbsolutePath().replace(File.separatorChar, '/'));
    }

    private void createTable(String stagingTableName, String stagingTableFields) {
        String[] columnNames = stagingTableFields.split(",");
        // Drop table of the same name
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + stagingTableName);
        // Create table
        StringBuilder createSQL = new StringBuilder();
        createSQL.append(String.format("CREATE TABLE IF NOT EXISTS %s (", stagingTableName));
        createSQL.append("id INT AUTO_INCREMENT NOT NULL,");
        for (String columnName : columnNames) {
            createSQL.append(columnName).append(" VARCHAR(255) ").append(',');
        }
        createSQL.append("created_at DATETIME NULL,");
        createSQL.append(String.format("CONSTRAINT pk_%s PRIMARY KEY (id))", stagingTableName));
        jdbcTemplate.execute(createSQL.toString());
        logProcess(ProcessLogging.LogLevel.DEBUG, "Create new table successfully.");
    }

    private void loadIntoTable(String stagingTableName, String stagingTableFields, String csvFileURL) {
        StringBuilder loadDataSQL = new StringBuilder();
        loadDataSQL.append(String.format("LOAD DATA INFILE \"%s\" INTO TABLE %s", csvFileURL, stagingTableName))
                .append(" FIELDS TERMINATED BY ',' ")
                .append(" ENCLOSED BY '\"' ")
                .append(" LINES TERMINATED BY '\\n' ")
                .append(" IGNORE 1 LINES")
                .append(String.format(" (%s) ", stagingTableFields))
                .append(" SET created_at = NOW()");
        jdbcTemplate.execute(loadDataSQL.toString());
        logProcess(ProcessLogging.LogLevel.DEBUG, "Loaded yay!");
    }
}
