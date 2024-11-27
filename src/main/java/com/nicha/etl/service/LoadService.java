package com.nicha.etl.service;

import com.nicha.etl.entity.config.ProcessLogging;
import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.repository.config.ProcessTrackerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoadService {

    private final LoggingService loggingService;
    private final JdbcTemplate jdbcTemplate;

    private final ProcessTrackerRepository processTrackerRepository;
    private ProcessTracker currentProcessTracker;

    @Autowired
    public LoadService(LoggingService loggingService,
                       ProcessTrackerRepository processTrackerRepository,
                       JdbcTemplate jdbcTemplate) {
        this.loggingService = loggingService;
        this.jdbcTemplate = jdbcTemplate;
        this.processTrackerRepository = processTrackerRepository;
        this.currentProcessTracker = this.processTrackerRepository.findByProcessName("Load Data");
        if (this.currentProcessTracker == null) {
            this.currentProcessTracker = new ProcessTracker();
            this.currentProcessTracker.setProcessName("Load Data");
            this.currentProcessTracker.setStatus(ProcessTracker.ProcessStatus.W_RI);
            this.processTrackerRepository.save(this.currentProcessTracker);
        }
    }

    public void loadDataToWarehouse() {
        loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.INFO, "Starting data loading to warehouse");

        if (this.currentProcessTracker.getStatus() == ProcessTracker.ProcessStatus.W_I) {
            loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.ERROR, "The crawling process is running at the moment.");
            return;
        }
        if (this.currentProcessTracker.lastStartedToday() && this.currentProcessTracker.getStatus() == ProcessTracker.ProcessStatus.W_SI) {
            loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.ERROR, "The crawling has been done for the day.");
            return;
        }

        try {
//            1. Kiểm tra xem dữ liệu khi insert vào dw có hoàn toàn mới hay không. Nếu có -> insert
//            2. Nếu dữ liệu được cập nhật -> B1: Thay đổi dòng dữ liệu trong dw với: isDelete = True, expried_date = NOW(), date_delete = NOW()
//            B2: Insert dòng dữ liệu có thay đổi vào dw
            jdbcTemplate.execute("CALL load_data_to_warehouse()");

            loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.INFO, "Successfully loaded data to warehouse");

        } catch (Exception e) {
            loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.ERROR, "Error loading data to warehouse: " + e.getMessage());
        }
    }
}
