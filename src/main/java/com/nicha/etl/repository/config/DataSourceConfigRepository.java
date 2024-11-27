package com.nicha.etl.repository.config;

import com.nicha.etl.entity.config.DataSourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DataSourceConfigRepository extends JpaRepository<DataSourceConfig, Long> {

}
