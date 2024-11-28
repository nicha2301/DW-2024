package com.nicha.etl.service;

import com.nicha.etl.entity.config.ProcessLogging;
import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.repository.config.ProcessTrackerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class ETLService {

    private final CrawlService crawlService;
    private final CleanService cleanService;
    private final LoadService loadService;
    private final LoggingService loggingService;
    private final ProcessTrackerRepository trackerRepo;

    private ProcessTracker tracker;

    public ETLService(CrawlService crawlService,
                      CleanService cleanService,
                      LoadService loadService,
                      LoggingService loggingService,
                      ProcessTrackerRepository trackerRepo) {
        this.crawlService = crawlService;
        this.cleanService = cleanService;
        this.loadService = loadService;
        this.loggingService = loggingService;
        this.trackerRepo = trackerRepo;

        tracker = this.trackerRepo.findByProcessName(getClass().getName());
        if (tracker == null) {
            tracker = new ProcessTracker();
            tracker.setProcessName(getClass().getName());
            tracker.setStatus(ProcessTracker.ProcessStatus.P_RR);
            this.trackerRepo.save(tracker);
        }
    }

    private boolean checkRunnableToday(boolean forced) {
        if (tracker == null)
            return false;
        if (tracker.getStatus() == ProcessTracker.ProcessStatus.P_R) {
            this.loggingService.logProcess(tracker, ProcessLogging.LogLevel.ERROR, "Có tiến trình khác đang chạy cùng service này, hủy.");
            return false;
        }
        // Bước 2: Kiểm tra process đã chạy thành công hôm nay chưa
        if (!forced && tracker.lastStartedToday() && tracker.getStatus() == ProcessTracker.ProcessStatus.P_SR) {
            this.loggingService.logProcess(tracker, ProcessLogging.LogLevel.ERROR, "Tiến trình này đã chạy thành công hôm nay rồi, hủy.");
            return false;
        }
        return true;
    }

    // tham số forceRun: bypass điều kiện chặn process chạy cùng ngày đã thành công
    public void runETLProcess(boolean forceRun) {
        // Bước 1: Kiểm tra process có cho phép được chạy không?
        boolean allowedToRun = checkRunnableToday(forceRun);
        if (!allowedToRun) {
            return;
        }

        // Bước 2: Đặt trạng thái là đang chạy để khóa client khác chạy cùng tiến trình
        Timestamp start = new Timestamp(System.currentTimeMillis());
        loggingService.logProcess(tracker, ProcessLogging.LogLevel.INFO, "ETL process begins", start, start);
        tracker.setStatus(ProcessTracker.ProcessStatus.P_R);
        tracker.setStartTime(start);
        trackerRepo.save(tracker);

        // Bước 3: Chạy các process nào:
        // - nếu lỗi thì log lỗi đồng thời cập nhật trạng thái process là Failure
        // - nếu chạy trơn tru hết thì cập nhật trạng thái process là Success và log
        try {
            // Bắt đầu quy trình ETL
            // 1. Crawl Data
            crawlService.crawlDataSourcesAndSaveToStaging(forceRun);

            // 2. Clean Data
            cleanService.cleanData();

            // 3. Load Data to Warehouse
            loadService.loadDataToWarehouse();

            // Kết thúc quá trình ETL
            tracker.setStatus(ProcessTracker.ProcessStatus.P_SR);
            tracker.setEndTime(new Timestamp(System.currentTimeMillis()));
            trackerRepo.save(tracker);
            loggingService.logProcess(tracker, ProcessLogging.LogLevel.INFO, "ETL process ended successfully", start, new Timestamp(System.currentTimeMillis()));

        } catch (Exception e) {
            tracker.setStatus(ProcessTracker.ProcessStatus.P_FR);
            tracker.setEndTime(new Timestamp(System.currentTimeMillis()));
            trackerRepo.save(tracker);
            loggingService.logProcess(tracker, ProcessLogging.LogLevel.ERROR, "ETL process ended with error ".concat(e.getLocalizedMessage()), start, new Timestamp(System.currentTimeMillis()));
        }
    }
}
