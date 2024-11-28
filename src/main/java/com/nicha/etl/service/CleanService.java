package com.nicha.etl.service;

import com.nicha.etl.entity.Product;
import com.nicha.etl.entity.StagingHeadPhone;
import com.nicha.etl.entity.StagingHeadPhoneDaily;
import com.nicha.etl.repository.ProductRepository;
import com.nicha.etl.repository.StagingHeadPhoneDailyRepository;
import com.nicha.etl.repository.StagingHeadPhoneRepository;
import io.netty.util.internal.StringUtil;
import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.repository.defaults.StagingHeadPhoneDailyRepository;
import com.nicha.etl.repository.defaults.StagingHeadPhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

@Service
public class CleanService {

    private final LoggingService loggingService;
    private final StagingHeadPhoneRepository stagingHeadPhoneRepository;
    private final StagingHeadPhoneDailyRepository stagingHeadPhoneDailyRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CleanService(LoggingService loggingService,
                        StagingHeadPhoneRepository stagingHeadPhoneRepository,
                        StagingHeadPhoneDailyRepository stagingHeadPhoneDailyRepository, JdbcTemplate jdbcTemplate) {
        this.loggingService = loggingService;
        this.stagingHeadPhoneRepository = stagingHeadPhoneRepository;
        this.stagingHeadPhoneDailyRepository = stagingHeadPhoneDailyRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void cleanData() {
        Timestamp start = new Timestamp(System.currentTimeMillis());
//        loggingService.logProcess("Clean Data", ProcessTracker.LogStatus.TRACING, "Starting data cleaning from staging table", start, start);
        try {
            jdbcTemplate.execute("CALL transform_and_load_staging_data()");
            loggingService.logProcess("Clean Data", "Successfully cleaned data", "SUCCESS");
        } catch (Exception e) {
            loggingService.logProcess("Clean Data", "Error cleaning data: " + e.getMessage(), "ERROR");
        }
    }
}
