package com.nicha.etl.service;

import com.nicha.etl.repository.config.ProcessTrackerRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoadService extends AbstractEtlService {

    private final JdbcTemplate jdbcTemplate;

    protected LoadService(LoggingService loggingService, ProcessTrackerRepository trackerRepo, JdbcTemplate jdbcTemplate) {
        super(loggingService, trackerRepo);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void process(boolean forcedRun) {
        jdbcTemplate.execute("CALL load_data_to_warehouse()");
    }
}
