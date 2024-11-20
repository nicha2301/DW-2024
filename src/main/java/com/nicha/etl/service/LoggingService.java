package com.nicha.etl.service;

import com.nicha.etl.entity.EtlLog;
import com.nicha.etl.repository.EtlLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class LoggingService {

    @Autowired
    private EtlLogRepository etlLogRepository;

    public void logProcess(String processName, String message, String status) {
        EtlLog log = new EtlLog();
        log.setProcessName(processName);
        log.setLogMessage(message);
        log.setStatus(status);
        log.setTimestamp(new Timestamp(System.currentTimeMillis()));
        etlLogRepository.save(log);
    }
}
