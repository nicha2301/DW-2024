package com.nicha.etl.controller;


import com.nicha.etl.service.ETLService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ETLScheduler {

    private final ETLService etlService;

    public ETLScheduler(ETLService etlService) {
        this.etlService = etlService;
    }

    // Lên lịch chạy ETL Process mỗi ngày vào 1h sáng
    @Scheduled(cron = "0 0 1 * * ?")  // Lịch chạy: mỗi ngày vào lúc 1h sáng
    public void scheduleETLProcess() {
        etlService.run(false);
    }
}
