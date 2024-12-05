package com.nicha.etl.service;

import com.nicha.etl.repository.config.ProcessTrackerRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransformCellphoneSAndLoadToStagingService extends AbstractEtlService {

    private final JdbcTemplate jdbcTemplate;

    protected TransformCellphoneSAndLoadToStagingService(LoggingService loggingService,
                                                         ProcessTrackerRepository trackerRepo,
                                                         JdbcTemplate jdbcTemplate) {
        super(loggingService, trackerRepo, "Transform CellphoneS Staging and Load to Global Staging");
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void process(boolean forcedRun) {
        jdbcTemplate.execute("CALL transform_and_load_staging_data()");
    }
}
