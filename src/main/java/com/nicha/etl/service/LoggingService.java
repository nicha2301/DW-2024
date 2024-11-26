package com.nicha.etl.service;

import com.nicha.etl.entity.config.ProcessLogging;
import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.repository.config.ProcessLogRepository;
import com.nicha.etl.repository.config.ProcessTrackerRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;

@Service
public class LoggingService {

    @Autowired
    private ProcessLogRepository processLogRepository;

    public void logProcess(ProcessTracker process, ProcessLogging.LogLevel status, String message) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logProcess(process, status, message, timestamp, timestamp);
    }

    public void logProcess(ProcessTracker process, ProcessLogging.LogLevel status, String message, Timestamp startTimestamp, Timestamp endTimestamp) {
        ProcessLogging logging = new ProcessLogging();
        logging.setLevel(status);
        logging.setMessage(message);
        logging.setProcessTracker(process);
        logging.setProcessStart(startTimestamp);
        logging.setProcessEnd(endTimestamp);
        logging.setDate(new Date(System.currentTimeMillis()));
        processLogRepository.save(logging);
    }
}
