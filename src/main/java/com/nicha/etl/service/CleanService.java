package com.nicha.etl.service;

import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.repository.config.ProcessTrackerRepository;
import com.nicha.etl.repository.defaults.StagingHeadPhoneDailyRepository;
import com.nicha.etl.repository.defaults.StagingHeadPhoneRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CleanService extends AbstractEtlService {

    private final StagingHeadPhoneRepository stagingHeadPhoneRepository;
    private final StagingHeadPhoneDailyRepository stagingHeadPhoneDailyRepository;
    private final JdbcTemplate jdbcTemplate;

    protected CleanService(LoggingService loggingService,
                           ProcessTrackerRepository trackerRepo,
                           StagingHeadPhoneRepository stagingHeadPhoneRepository,
                           StagingHeadPhoneDailyRepository stagingHeadPhoneDailyRepository, JdbcTemplate jdbcTemplate) {
        super(loggingService, trackerRepo, "Clean Data");
        this.stagingHeadPhoneRepository = stagingHeadPhoneRepository;
        this.stagingHeadPhoneDailyRepository = stagingHeadPhoneDailyRepository;
        this.jdbcTemplate = jdbcTemplate;
        ProcessTracker pt = this.trackerRepo.findByProcessName("Import to Staging");
        this.tracker.setRequiredProcess(pt);
        this.trackerRepo.save(this.tracker);
    }

    @Override
    protected void process(boolean forcedRun) {
        jdbcTemplate.execute("CALL transform_and_load_staging_data()");
    }
}
