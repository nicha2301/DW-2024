package com.nicha.etl.service;

import com.nicha.etl.entity.Product;
import com.nicha.etl.entity.StagingHeadPhoneDaily;
import com.nicha.etl.repository.ProductRepository;
import com.nicha.etl.repository.StagingHeadPhoneDailyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LoadService {

    private final LoggingService loggingService;
    private final ProductRepository productRepository;
    private final StagingHeadPhoneDailyRepository stagingHeadPhoneDailyRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public LoadService(LoggingService loggingService,
                       ProductRepository productRepository,
                       StagingHeadPhoneDailyRepository stagingHeadPhoneDailyRepository, JdbcTemplate jdbcTemplate) {
        this.loggingService = loggingService;
        this.productRepository = productRepository;
        this.stagingHeadPhoneDailyRepository = stagingHeadPhoneDailyRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void loadDataToWarehouse() {
        loggingService.logProcess("Load Data", "Starting data loading to warehouse", "IN_PROGRESS");

        try {
//            1. Kiểm tra xem dữ liệu khi insert vào dw có hoàn toàn mới hay không. Nếu có -> insert
//            2. Nếu dữ liệu được cập nhật -> B1: Thay đổi dòng dữ liệu trong dw với: isDelete = True, expried_date = NOW(), date_delete = NOW()
//            B2: Insert dòng dữ liệu có thay đổi vào dw
            jdbcTemplate.execute("CALL load_data_to_warehouse()");

            loggingService.logProcess("Load Data", "Successfully loaded data to warehouse", "SUCCESS");

        } catch (Exception e) {
            loggingService.logProcess("Load Data", "Error loading data to warehouse: " + e.getMessage(), "ERROR");
        }
    }
}
