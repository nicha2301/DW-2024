package com.nicha.etl.repository.defaults;

import com.nicha.etl.entity.defaults.StagingHeadPhoneDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StagingHeadPhoneDailyRepository extends JpaRepository<StagingHeadPhoneDaily, Long> {
}
