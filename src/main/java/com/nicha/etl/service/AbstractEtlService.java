package com.nicha.etl.service;

import com.nicha.etl.entity.config.ProcessLogging;
import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.repository.config.ProcessTrackerRepository;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * This class purpose is to hide process tracker and steps done
 * at the start and the end of each ETL tasks
 */
public abstract class AbstractEtlService implements IEtlService {

    private final LoggingService loggingService;
    protected final ProcessTrackerRepository trackerRepo;
    protected ProcessTracker tracker;
    protected String processName;

    protected AbstractEtlService(LoggingService loggingService,
                                 ProcessTrackerRepository trackerRepo,
                                 String processName) {
        this.loggingService = loggingService;
        this.trackerRepo = trackerRepo;
        this.processName = processName;
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
        this.tracker = this.trackerRepo.save(this.tracker);
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
        // If the tracker is not exists in database, create one and save it in database
        // Trust me, this tracker will be used throughout this script
        this.tracker = this.trackerRepo.findByProcessName(this.processName);
        // Pre-condition
        if (tracker.getStatus() == ProcessTracker.ProcessStatus.IN_PROGRESS)
            throw new RuntimeException(String.format("Process \"%s\" đang chạy bởi ai đó khác, hủy.", processName));

        // Bước 2: Kiểm tra process đã chạy thành công hôm nay chưa
        if (!forceRun && tracker.lastStartedToday() && tracker.getStatus() == ProcessTracker.ProcessStatus.SUCCESS)
            throw new RuntimeException(String.format("Tiến trình \"%s\" đã chạy thành công hôm nay rồi, hủy.", processName));

        ProcessTracker pt = tracker.getRequiredProcess();
        if (!forceRun && pt != null && pt.getStatus() != ProcessTracker.ProcessStatus.SUCCESS)
            throw new RuntimeException(String.format("Process tiên quyết \"%s\" chưa chạy thành công, hủy.", pt.getProcessName()));

        // Set state to IN-PROGRESS and log
        changeStatus(ProcessTracker.ProcessStatus.IN_PROGRESS);

        Timestamp start = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("mm:ss.SSS");
        try {
            // Log its starting point
            String startingMsg = String.format("Starting process \"%s\"", processName);
            logProcess(ProcessLogging.LogLevel.INFO, startingMsg, start, start);
            // Do process in this only method
            process(forceRun);
            // Set state to SUCCESS and log end because it was finished without exception
            Timestamp end = new Timestamp(System.currentTimeMillis());
            changeStatus(ProcessTracker.ProcessStatus.SUCCESS);
            String completedMsg = String.format("Completed process \"%s\" in: %ss", processName, format.format(end.getTime() - start.getTime()));
            logProcess(ProcessLogging.LogLevel.INFO, completedMsg, start, end);
        }
        catch (Exception e) {
            // When exception is thrown, set state to FAILURE and log it
            Timestamp end = new Timestamp(System.currentTimeMillis());
            changeStatus(ProcessTracker.ProcessStatus.FAILED);
            String errorMsg = String.format("Error while processing \"%s\": %s", processName, e.getMessage());
            logProcess(ProcessLogging.LogLevel.ERROR, errorMsg, start, end);
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }
}
