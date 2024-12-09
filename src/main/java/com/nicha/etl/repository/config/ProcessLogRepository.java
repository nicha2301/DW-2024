package com.nicha.etl.repository.config;

import com.nicha.etl.entity.config.ProcessLogging;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessLogRepository extends JpaRepository<ProcessLogging, Long> {

    Page<ProcessLogging> findAll(Pageable pageable);

    Page<ProcessLogging> findAllBy(Pageable pageable);
}
