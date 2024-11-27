package com.nicha.etl.service;

import com.nicha.etl.entity.config.DataSourceConfig;
import com.nicha.etl.entity.config.ProcessLogging;
import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.entity.defaults.StagingHeadPhone;
import com.nicha.etl.repository.config.DataSourceConfigRepository;
import com.nicha.etl.repository.config.ProcessTrackerRepository;
import com.nicha.etl.repository.defaults.ProductRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.List;

@Service
public class CrawlService {

    private final LoggingService loggingService;
    private final ProcessTrackerRepository processTrackerRepository;
    private final DataSourceConfigRepository dataSourceConfigRepository;
    private final ProductRepository productRepository;
    private ProcessTracker currentProcessTracker;

    @Autowired
    public CrawlService(LoggingService loggingService, ProcessTrackerRepository processTrackerRepository, DataSourceConfigRepository dataSourceConfigRepository, ProductRepository productRepository) {
        this.loggingService = loggingService;
        this.dataSourceConfigRepository = dataSourceConfigRepository;
        this.processTrackerRepository = processTrackerRepository;

        this.currentProcessTracker = this.processTrackerRepository.findByProcessName(getClass().getName());
        if (this.currentProcessTracker == null) {
            this.currentProcessTracker = new ProcessTracker();
            this.currentProcessTracker.setProcessName(getClass().getName());
            this.currentProcessTracker.setStatus(ProcessTracker.ProcessStatus.C_RE);
            this.processTrackerRepository.save(this.currentProcessTracker);
        }
        this.productRepository = productRepository;
    }

    public void crawlDataSourcesAndSaveToStaging() throws Exception {
        // Bước 1: Kiểm tra process có đang chạy không?
        if (this.currentProcessTracker.getStatus() == ProcessTracker.ProcessStatus.C_E) {
            this.loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.ERROR, "Có tiến trình khác đang chạy cùng service này, hủy.");
            throw new RuntimeException("Có tiến trình khác đang chạy cùng service này");
        }
        // Bước 2: Kiểm tra process đã chạy thành công hôm nay chưa
        if (this.currentProcessTracker.lastStartedToday() && this.currentProcessTracker.getStatus() == ProcessTracker.ProcessStatus.C_SE) {
            this.loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.ERROR, "Tiến trình này đã chạy thành công hôm nay rồi, hủy.");
            throw new RuntimeException("Tiến trình này đã chạy thành công hôm nay rồi, hủy.");
        }

        // Bước 3: Đổi trạng thái process sang Crawl + Log process bắt đầu
        Timestamp start = new Timestamp(System.currentTimeMillis());
        this.currentProcessTracker.setStatus(ProcessTracker.ProcessStatus.C_E);
        this.currentProcessTracker.setStartTime(start);
        this.processTrackerRepository.save(this.currentProcessTracker);

        //Bước 4: Làm hành động nào đó
        this.loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.INFO, "Getting all data source config to crawl.");
        List<DataSourceConfig> configs = this.dataSourceConfigRepository.findAll();

        // Iterate each config and work on them
        for (DataSourceConfig config : configs) {
            this.loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.INFO, "DataSource: " + config.toString(), start, new Timestamp(System.currentTimeMillis()));
        }

        // Bước 5: Khi hoàn thiện, đổi trạng thái thành Success, hoặc Failure và log kết thúc
        this.currentProcessTracker.setStatus(ProcessTracker.ProcessStatus.C_SE);
        this.currentProcessTracker.setEndTime(new Timestamp(System.currentTimeMillis()));
        this.processTrackerRepository.save(this.currentProcessTracker);
        this.loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.INFO, "Getting all data source config to crawl.", start, new Timestamp(System.currentTimeMillis()));
    }


    private List<StagingHeadPhone> processResponseData(String responseBody) {
        List<StagingHeadPhone> productList = new ArrayList<>();
        JSONObject jsonResponse = new JSONObject(responseBody);
        JSONArray products = jsonResponse.getJSONObject("data").getJSONArray("products");

        for (int i = 0; i < products.length(); i++) {
            JSONObject productJson = products.getJSONObject(i);
            JSONObject general = productJson.getJSONObject("general");
            JSONObject attributes = general.getJSONObject("attributes");
            JSONObject filterable = productJson.optJSONObject("filterable");

            StagingHeadPhone product = new StagingHeadPhone();
            product.setProductId(general.optString("product_id"));
            product.setName(general.optString("name"));
            product.setBrand(attributes.optString("phone_accessory_brands"));
            product.setType(attributes.optString("mobile_accessory_type"));
            product.setPrice(filterable != null ? filterable.optString("price") : "");
            product.setWarrantyInfo(attributes.optString("warranty_information"));
            product.setFeature(attributes.optString("tai_nghe_tinh_nang"));
            product.setVoiceControl(attributes.optString("tai_nghe_dieu_khien"));
            product.setMicrophone(attributes.optString("tai_nghe_micro"));
            product.setBatteryLife(attributes.optString("tai_nghe_pin"));
            product.setDimensions(attributes.optString("dimensions", attributes.optString("tai_nghe_kich_thuoc_driver", attributes.optString("tai_nghe_do_dai_day", ""))));
            product.setWeight(attributes.optString("product_weight"));
            product.setCompatibility(attributes.optString("tai_nghe_tuong_thich"));
            product.setCreatedAt(String.valueOf(LocalDateTime.now()));

            productList.add(product);
        }

        return productList;
    }

}
