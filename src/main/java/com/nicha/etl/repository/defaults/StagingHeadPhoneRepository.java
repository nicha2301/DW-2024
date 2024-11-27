package com.nicha.etl.repository.defaults;

import com.nicha.etl.entity.defaults.StagingHeadPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface StagingHeadPhoneRepository extends JpaRepository<StagingHeadPhone, Long> {
//    @Query(value = "SELECT * FROM staging_head_phone", nativeQuery = true)
//    List<Map<String, Object>> fetchStagingData();
}
