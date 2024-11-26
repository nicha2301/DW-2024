package com.nicha.etl.repository.config;

import com.nicha.etl.entity.config.ProcessLogging;
import com.nicha.etl.entity.config.ProcessTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessTrackerRepository extends JpaRepository<ProcessTracker, Long> {
    ProcessTracker findByProcessName(String processName);
}
