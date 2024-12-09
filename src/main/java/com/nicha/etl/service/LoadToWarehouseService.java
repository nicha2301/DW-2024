package com.nicha.etl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoadToWarehouseService extends AbstractEtlService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    protected LoadToWarehouseService(LoggingService loggingService, ProcessTrackerService trackerService, JdbcTemplate jdbcTemplate) {
        super(loggingService, trackerService, "Load From Staging To Warehouse");
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void process(boolean forcedRun) {
        jdbcTemplate.execute("CALL load_data_to_warehouse()");
    }
}
