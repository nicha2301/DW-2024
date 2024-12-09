package com.nicha.etl.service;

import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.repository.config.ProcessTrackerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class ProcessTrackerService {

    private final ProcessTrackerRepository processTrackerRepository;

    @Autowired
    public ProcessTrackerService(ProcessTrackerRepository processTrackerRepository) {
        this.processTrackerRepository = processTrackerRepository;
    }

    public List<ProcessTracker> getAllProcessTrackers() {
        return processTrackerRepository.findAll();
    }

    public ProcessTracker getProcessTrackerByName(String processTrackerName) {
        return processTrackerRepository.findByProcessName(processTrackerName);
    }

    /**
     * This method serves as a way to change the process status, it will change
     * status and save it to database
     * - If changed to IN_PROGRESS, the start time will be set to current time
     * - If the based state was IN_PROGRESS, the end time will be set to current time
     * @param newStatus Status to be changed into
     */
    public ProcessTracker changeProcessStatus(String processTrackerName, ProcessTracker.ProcessStatus newStatus) {
        ProcessTracker tracker = getProcessTrackerByName(processTrackerName);
        if (tracker == null) {
            return null;
        }

        ProcessTracker.ProcessStatus oldStatus = tracker.getStatus();
        tracker.setStatus(newStatus);
        if (newStatus == ProcessTracker.ProcessStatus.IN_PROGRESS)
            tracker.setStartTime(new Timestamp(System.currentTimeMillis()));
        else if (oldStatus == ProcessTracker.ProcessStatus.IN_PROGRESS)
            tracker.setEndTime(new Timestamp(System.currentTimeMillis()));
        tracker = processTrackerRepository.save(tracker);
        return tracker;
    }
}
