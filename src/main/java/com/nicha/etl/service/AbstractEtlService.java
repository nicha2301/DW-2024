package com.nicha.etl.service;

import com.nicha.etl.entity.config.ProcessLogging;
import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.repository.config.ProcessTrackerRepository;

import java.sql.Timestamp;
import java.text.DateFormat;

/**
 * This class purpose is to hide process tracker and steps done
 * at the start and the end of each ETL tasks
 */
public abstract class AbstractEtlService {

    private final LoggingService loggingService;
    private final ProcessTrackerRepository trackerRepo;
    private ProcessTracker tracker;

    protected AbstractEtlService(LoggingService loggingService, ProcessTrackerRepository trackerRepo) {
        this.loggingService = loggingService;
        this.trackerRepo = trackerRepo;

        // If the tracker is not exists in database, create one and save it in database
        // Trust me, this tracker will be used throughout this script
        this.tracker = this.trackerRepo.findByProcessName(getClass().getName());
        if (this.tracker == null) {
            this.tracker = new ProcessTracker();
            this.tracker.setProcessName(getClass().getName());
            this.tracker.setStatus(ProcessTracker.ProcessStatus.READY);
            this.trackerRepo.save(tracker);
        }
    }

    /**
     * This method serves as a way to change the process status, it will change
     * status and save it to database
     * - If changed to IN_PROGRESS, the start time will be set to current time
     * - If the based state was IN_PROGRESS, the end time will be set to current time
     * @param newStatus Status to be changed into
     */
    protected void changeStatus(ProcessTracker.ProcessStatus newStatus) {
        ProcessTracker.ProcessStatus oldStatus = this.tracker.getStatus();
        this.tracker.setStatus(newStatus);
        if (newStatus == ProcessTracker.ProcessStatus.IN_PROGRESS)
            this.tracker.setStartTime(new Timestamp(System.currentTimeMillis()));
        else if (oldStatus == ProcessTracker.ProcessStatus.IN_PROGRESS)
            this.tracker.setEndTime(new Timestamp(System.currentTimeMillis()));
        this.trackerRepo.save(this.tracker);
    }

    /**
     * Log the process to both database and console
     * @param level One of the following {@link com.nicha.etl.entity.config.ProcessLogging.LogLevel#DEBUG},
     *              {@link com.nicha.etl.entity.config.ProcessLogging.LogLevel#INFO},
     *              {@link com.nicha.etl.entity.config.ProcessLogging.LogLevel#WARN},
     *              {@link com.nicha.etl.entity.config.ProcessLogging.LogLevel#ERROR}
     * @param message Log message
     */
    protected void logProcess(ProcessLogging.LogLevel level, String message) {
        this.loggingService.logProcess(this.tracker, level, message);
    }

    /**
     * Log the process to both database and console
     * @param level One of the following {@link com.nicha.etl.entity.config.ProcessLogging.LogLevel#DEBUG},
     *              {@link com.nicha.etl.entity.config.ProcessLogging.LogLevel#INFO},
     *              {@link com.nicha.etl.entity.config.ProcessLogging.LogLevel#WARN},
     *              {@link com.nicha.etl.entity.config.ProcessLogging.LogLevel#ERROR}
     * @param message Log message
     * @param start Process start timestamp
     * @param end {Process end timestamp}
     */
    protected void logProcess(ProcessLogging.LogLevel level, String message, Timestamp start, Timestamp end) {
        this.loggingService.logProcess(this.tracker, level, message, start, end);
    }

    /**
     * Check if the run request is allowed
     * @param forced boolean, if true, it will ignore the prevention from successful run today
     */
    private boolean checkRunnableToday(boolean forced) {
        if (tracker == null)
            return false;
        if (tracker.getStatus() == ProcessTracker.ProcessStatus.IN_PROGRESS) {
            this.loggingService.logProcess(tracker, ProcessLogging.LogLevel.ERROR, "Có tiến trình khác đang chạy cùng service này, hủy.");
            return false;
        }
        // Bước 2: Kiểm tra process đã chạy thành công hôm nay chưa
        if (!forced && tracker.lastStartedToday() && tracker.getStatus() == ProcessTracker.ProcessStatus.SUCCESS) {
            this.loggingService.logProcess(tracker, ProcessLogging.LogLevel.ERROR, "Tiến trình này đã chạy thành công hôm nay rồi, hủy.");
            return false;
        }
        return true;
    }

    /**
     * The main method it will process when running the {@link #run(boolean) run(forceRun)} method
     * Depend on how the method was implemented, forceRun can be ignored
     * @apiNote Run this method instead of {@link #run(boolean) run(forceRun)} method
     * will ignore the starting check, the state change at the beginning and ending of
     * the run.
     * @param forceRun state of forceRun from the {@link #run(boolean) run} method
     */
    protected abstract void process(boolean forceRun);

    /**
     * The RECOMMENDED method to run the ETL tasks
     * @param forceRun boolean, if true the process will bypass the successful run of today
     *                 and keep running
     */
    public void run(boolean forceRun) {
        // Pre-condition
        boolean ready = checkRunnableToday(forceRun);
        if (!ready) {
            return;
        }
        // Set state to IN-PROGRESS and log
        changeStatus(ProcessTracker.ProcessStatus.IN_PROGRESS);
        Timestamp start = new Timestamp(System.currentTimeMillis());
        try {
            // Log its starting point
            String startingMsg = String.format("Starting process %s", tracker.getProcessName());
            logProcess(ProcessLogging.LogLevel.INFO, startingMsg, start, start);
            // Do process in this only method
            process(forceRun);
            // Set state to SUCCESS and log end because it was finished without exception
            Timestamp end = new Timestamp(System.currentTimeMillis());
            changeStatus(ProcessTracker.ProcessStatus.SUCCESS);
            String completedMsg = String.format("Completed process %s in: %s", tracker.getProcessName(), DateFormat.getTimeInstance().format(end.getNanos() - start.getNanos()));
            logProcess(ProcessLogging.LogLevel.INFO, completedMsg, start, end);
        }
        catch (Exception e) {
            // When exception is thrown, set state to FAILURE and log it
            Timestamp end = new Timestamp(System.currentTimeMillis());
            changeStatus(ProcessTracker.ProcessStatus.FAILED);
            String errorMsg = String.format("Error while processing %s: %s", tracker.getProcessName(), e.getMessage());
            logProcess(ProcessLogging.LogLevel.ERROR, errorMsg, start, end);
        }
    }
}
