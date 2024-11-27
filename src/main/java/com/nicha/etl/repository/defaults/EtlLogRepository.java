package com.nicha.etl.repository.defaults;

import com.nicha.etl.entity.defaults.EtlLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtlLogRepository extends JpaRepository<EtlLog, Long> {
}