package com.nicha.etl.service;

import com.nicha.etl.entity.config.DataSourceConfig;
import com.nicha.etl.entity.config.ProcessLogging;
import com.nicha.etl.repository.config.DataSourceConfigRepository;
import com.nicha.etl.repository.config.ProcessTrackerRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class ImportToStagingService extends AbstractEtlService {

    private final JdbcTemplate jdbcTemplate;
    private final DataSourceConfigRepository dataSourceConfigRepository;

    protected ImportToStagingService(LoggingService loggingService,
                                     ProcessTrackerRepository trackerRepo, JdbcTemplate jdbcTemplate, DataSourceConfigRepository dataSourceConfigRepository) {
        super(loggingService, trackerRepo);
        this.jdbcTemplate = jdbcTemplate;
        this.dataSourceConfigRepository = dataSourceConfigRepository;
    }

    @Override
    protected void process(boolean forcedRun) {
        logProcess(ProcessLogging.LogLevel.INFO, "Getting all data source config to load to staging.");
        List<DataSourceConfig> configs = this.dataSourceConfigRepository.findAll();

        // Bước 4: Làm việc với từng Configs
        for (DataSourceConfig dataSourceConfig : configs) {
            logProcess(ProcessLogging.LogLevel.INFO, "Working with data source: " + dataSourceConfig.getName());
            loadToStaging(dataSourceConfig);
            logProcess(ProcessLogging.LogLevel.INFO, "Completed load with data source: " + dataSourceConfig.getName());
        }
    }

    private void loadToStaging(DataSourceConfig dataSourceConfig) {
        String csvFileURL = dataSourceConfig.getCrawlSaveLocation();
        if (csvFileURL == null) {
            logProcess(ProcessLogging.LogLevel.ERROR, "Crawl save location not found.");
            return;
        }
        File csvFile = new File(csvFileURL);
        if (!csvFile.exists()) {
            logProcess(ProcessLogging.LogLevel.ERROR, "Crawl save file not found.");
            return;
        }
        String stagingTableName = dataSourceConfig.getStagingTableName();
        if (stagingTableName == null || stagingTableName.trim().isEmpty()) {
            logProcess(ProcessLogging.LogLevel.ERROR, "Staging table name not found.");
            return;
        }
        String stagingTableFields = dataSourceConfig.getStagingFields();
        if (stagingTableFields == null || stagingTableFields.trim().isEmpty()) {
            logProcess(ProcessLogging.LogLevel.ERROR, "Staging table fields not found.");
            return;
        }
        createTable(stagingTableName, stagingTableFields);
        loadIntoTable(stagingTableName, stagingTableFields, csvFile.getAbsolutePath().replace(File.separatorChar, '/'));
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
        logProcess(ProcessLogging.LogLevel.INFO, "Loaded yay!");
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
        logProcess(ProcessLogging.LogLevel.INFO, "Create new table successfully.");
    }
}
