package com.nicha.etl.service;

import com.nicha.etl.entity.config.DataSourceConfig;
import com.nicha.etl.entity.config.ProcessLogging;
import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.entity.defaults.StagingHeadPhone;
import com.nicha.etl.repository.config.DataSourceConfigRepository;
import com.nicha.etl.repository.config.ProcessTrackerRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

@Service
public class CrawlService {

    private final LoggingService loggingService;
    private final ProcessTrackerRepository processTrackerRepository;
    private final DataSourceConfigRepository dataSourceConfigRepository;
    private ProcessTracker currentProcessTracker;

    @Autowired
    public CrawlService(LoggingService loggingService, ProcessTrackerRepository processTrackerRepository, DataSourceConfigRepository dataSourceConfigRepository) {
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
    }

    private boolean sameDate(Timestamp timestamp1, Timestamp timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        if (timestamp1 == null || timestamp2 == null)
            return timestamp1 == timestamp2;
        cal1.setTime(timestamp1);
        cal2.setTime(timestamp2);
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

    public void crawlDataSourcesAndSaveToStaging() {
        if (this.currentProcessTracker.getStatus() == ProcessTracker.ProcessStatus.C_E) {
            loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.ERROR, "The crawling process is running at the moment.");
            return;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (sameDate(now, this.currentProcessTracker.getStartTime()) && this.currentProcessTracker.getStatus() == ProcessTracker.ProcessStatus.C_SE) {
            loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.ERROR, "The crawling has been done for the day.");
            return;
        }

        this.currentProcessTracker.setStatus(ProcessTracker.ProcessStatus.C_E);
        this.currentProcessTracker.setStartTime(new Timestamp(System.currentTimeMillis()));
        this.processTrackerRepository.save(this.currentProcessTracker);

        loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.INFO, "Getting all data source config to crawl.");
        List<DataSourceConfig> configs = this.dataSourceConfigRepository.findAll();

        // Iterate and work on them
        for (DataSourceConfig config : configs) {
            loggingService.logProcess(this.currentProcessTracker, ProcessLogging.LogLevel.INFO, "DataSource: " + config.toString());
        }

        this.currentProcessTracker.setStatus(ProcessTracker.ProcessStatus.C_SE);
        this.currentProcessTracker.setEndTime(new Timestamp(System.currentTimeMillis()));
        this.processTrackerRepository.save(this.currentProcessTracker);
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
