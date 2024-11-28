package com.nicha.etl.repository.config;

import com.nicha.etl.entity.config.ProcessLogging;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessLogRepository extends JpaRepository<ProcessLogging, Long> {
}
