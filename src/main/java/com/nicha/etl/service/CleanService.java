package com.nicha.etl.service;

import com.nicha.etl.entity.Product;
import com.nicha.etl.entity.StagingHeadPhone;
import com.nicha.etl.entity.StagingHeadPhoneDaily;
import com.nicha.etl.repository.ProductRepository;
import com.nicha.etl.repository.StagingHeadPhoneDailyRepository;
import com.nicha.etl.repository.StagingHeadPhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CleanService {

    private final LoggingService loggingService;
    private final StagingHeadPhoneRepository stagingHeadPhoneRepository;
    private final StagingHeadPhoneDailyRepository stagingHeadPhoneDailyRepository;

    @Autowired
    public CleanService(LoggingService loggingService,
                        StagingHeadPhoneRepository stagingHeadPhoneRepository,
                        StagingHeadPhoneDailyRepository stagingHeadPhoneDailyRepository) {
        this.loggingService = loggingService;
        this.stagingHeadPhoneRepository = stagingHeadPhoneRepository;
        this.stagingHeadPhoneDailyRepository = stagingHeadPhoneDailyRepository;
    }

    public void cleanData() {
        loggingService.logProcess("Clean Data", "Starting data cleaning from staging table", "IN_PROGRESS");

        try {
            // Lấy dữ liệu từ bảng staging
            List<StagingHeadPhone> rawData = stagingHeadPhoneRepository.findAll();
            List<StagingHeadPhoneDaily> cleanedData = new ArrayList<>();

            for (StagingHeadPhone record : rawData) {
                // Làm sạch và chuyển đổi dữ liệu
                StagingHeadPhoneDaily product = cleanAndTransform(record);

                if (product != null) {
                    cleanedData.add(product);
                }
            }

            // Lưu dữ liệu đã làm sạch vào bảng `Product`
            stagingHeadPhoneDailyRepository.saveAll(cleanedData);

            loggingService.logProcess("Clean Data", "Successfully cleaned data", "SUCCESS");

        } catch (Exception e) {
            loggingService.logProcess("Clean Data", "Error cleaning data: " + e.getMessage(), "ERROR");
        }
    }

    private StagingHeadPhoneDaily cleanAndTransform(StagingHeadPhone record) {
        try {
            // Kiểm tra điều kiện dữ liệu hợp lệ
            if (record.getName() == null || record.getName().isEmpty() ||
                    record.getPrice() == null || record.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return null; // Bỏ qua record không hợp lệ
            }

            // Cast từ StagingHeadPhone sang StagingHeadPhoneDaily
            // Xoa cac the? html
            // ...
            //todo
            

            return null;
        } catch (Exception e) {
            // Ghi log lỗi khi xử lý record
            loggingService.logProcess("Clean Data", "Error processing record: " + e.getMessage(), "ERROR");
            return null;
        }
    }
}
