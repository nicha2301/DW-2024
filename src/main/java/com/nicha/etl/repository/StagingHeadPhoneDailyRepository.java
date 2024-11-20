package com.nicha.etl.repository;

import com.nicha.etl.entity.StagingHeadPhoneDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StagingHeadPhoneDailyRepository extends JpaRepository<StagingHeadPhoneDaily, Long> {
}
