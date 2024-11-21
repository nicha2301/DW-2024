package com.nicha.etl.service;

import com.nicha.etl.entity.Product;
import com.nicha.etl.entity.StagingHeadPhoneDaily;
import com.nicha.etl.repository.ProductRepository;
import com.nicha.etl.repository.StagingHeadPhoneDailyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LoadService {

    private final LoggingService loggingService;
    private final ProductRepository productRepository;
    private final StagingHeadPhoneDailyRepository stagingHeadPhoneDailyRepository;

    @Autowired
    public LoadService(LoggingService loggingService,
                       ProductRepository productRepository,
                       StagingHeadPhoneDailyRepository stagingHeadPhoneDailyRepository) {
        this.loggingService = loggingService;
        this.productRepository = productRepository;
        this.stagingHeadPhoneDailyRepository = stagingHeadPhoneDailyRepository;
    }

    public void loadDataToWarehouse() {
        loggingService.logProcess("Load Data", "Starting data loading to warehouse", "IN_PROGRESS");

        try {
            // Lấy dữ liệu từ bảng staging cleaned (StagingHeadPhoneDaily)
            List<StagingHeadPhoneDaily> cleanedData = stagingHeadPhoneDailyRepository.findAll();

            List<Product> warehouseData = new ArrayList<>();
            
            //todo
//            1. Kiểm tra xem dữ liệu khi insert vào dw có hoàn toàn mới hay không. Nếu có -> insert
//            2. Nếu dữ liệu được cập nhật -> B1: Thay đổi dòng dữ liệu trong dw với: isDelete = True, expried_date = NOW(), date_delete = NOW()
//            B2: Insert dòng dữ liệu có thay đổi vào dw


            loggingService.logProcess("Load Data", "Successfully loaded data to warehouse", "SUCCESS");

        } catch (Exception e) {
            loggingService.logProcess("Load Data", "Error loading data to warehouse: " + e.getMessage(), "ERROR");
        }
    }
}
