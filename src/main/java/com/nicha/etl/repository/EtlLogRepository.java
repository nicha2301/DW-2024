package com.nicha.etl.repository;

import com.nicha.etl.entity.EtlLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtlLogRepository extends JpaRepository<EtlLog, Long> {
}