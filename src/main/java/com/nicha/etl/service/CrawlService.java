package com.nicha.etl.service;

import com.nicha.etl.entity.config.DataSourceConfig;
import com.nicha.etl.entity.config.ProcessLogging;
import com.nicha.etl.entity.config.ProcessTracker;
import com.nicha.etl.entity.defaults.Product;
import com.nicha.etl.entity.defaults.StagingHeadPhone;
import com.nicha.etl.repository.config.DataSourceConfigRepository;
import com.nicha.etl.repository.config.ProcessTrackerRepository;
import com.nicha.etl.repository.defaults.StagingHeadPhoneRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service
public class CrawlService {

    private final Logger logger = LoggerFactory.getLogger(CrawlService.class);

    private final LoggingService loggingService;
    private final DataSourceConfigRepository dataSourceConfigRepository;
    private final StagingHeadPhoneRepository stagingHeadPhoneRepository;

    private final ProcessTrackerRepository trackerRepo;
    private ProcessTracker tracker;
    private HttpClient httpClient;
    private HttpRequest.Builder httpRequestBuilder;

    @Autowired
    public CrawlService(LoggingService loggingService,
                        ProcessTrackerRepository trackerRepo,
                        DataSourceConfigRepository dataSourceConfigRepository,
                        StagingHeadPhoneRepository stagingHeadPhoneRepository) {
        this.loggingService = loggingService;
        this.dataSourceConfigRepository = dataSourceConfigRepository;
        this.trackerRepo = trackerRepo;
        this.stagingHeadPhoneRepository = stagingHeadPhoneRepository;

        tracker = trackerRepo.findByProcessName(getClass().getName());
        if (tracker == null) {
            tracker = new ProcessTracker();
            tracker.setProcessName(getClass().getName());
            tracker.setStatus(ProcessTracker.ProcessStatus.C_RE);
            tracker = trackerRepo.save(this.tracker);
        }
        httpClient = HttpClient.newHttpClient();
        httpRequestBuilder = HttpRequest.newBuilder();
    }

    private boolean checkRunnableToday(boolean forced) {
        if (tracker == null)
            return false;
        if (tracker.getStatus() == ProcessTracker.ProcessStatus.C_E) {
            this.loggingService.logProcess(tracker, ProcessLogging.LogLevel.ERROR, "Có tiến trình khác đang chạy cùng crawl này, hủy.");
            return false;
        }
        // Bước 2: Kiểm tra process đã chạy thành công hôm nay chưa
        if (!forced && tracker.lastStartedToday() && tracker.getStatus() == ProcessTracker.ProcessStatus.C_SE) {
            this.loggingService.logProcess(tracker, ProcessLogging.LogLevel.ERROR, "Tiến trình này đã chạy thành công hôm nay rồi, hủy.");
            return false;
        }
        return true;
    }

    public void crawlDataSourcesAndSaveToStaging(boolean forceRun) {
        // Bước 1: Kiểm tra process có cho phép được chạy không?
        boolean allowedToRun = checkRunnableToday(forceRun);
        if (!allowedToRun) {
            return;
        }

        // Bước 2: Đổi trạng thái process sang Crawl + Log process bắt đầu
        Timestamp start = new Timestamp(System.currentTimeMillis());
        this.tracker.setStatus(ProcessTracker.ProcessStatus.C_E);
        this.tracker.setStartTime(start);
        this.tracker = this.trackerRepo.save(this.tracker);

        // Bước 3: Lấy danh sách data source config xd
        this.loggingService.logProcess(this.tracker, ProcessLogging.LogLevel.INFO, "Getting all data source config to crawl.");
        List<DataSourceConfig> configs = this.dataSourceConfigRepository.findAll();

        try {
            // Bước 4: Làm việc với từng Configs
            for (DataSourceConfig config : configs) {
                this.loggingService.logProcess(this.tracker, ProcessLogging.LogLevel.INFO, "DataSource: " + config.toString());
                processConfig(config);
            }

            // Bước 5: Khi hoàn thiện, đổi trạng thái thành Success, hoặc Failure và log kết thúc
            this.tracker.setStatus(ProcessTracker.ProcessStatus.C_SE);
            this.tracker.setEndTime(new Timestamp(System.currentTimeMillis()));
            this.tracker = this.trackerRepo.save(this.tracker);
            this.loggingService.logProcess(this.tracker, ProcessLogging.LogLevel.INFO, "Success.", start, new Timestamp(System.currentTimeMillis()));
        }
        catch (Exception e) {
            this.tracker.setStatus(ProcessTracker.ProcessStatus.C_FE);
            this.tracker.setEndTime(new Timestamp(System.currentTimeMillis()));
            this.tracker = this.trackerRepo.save(this.tracker);
            this.loggingService.logProcess(this.tracker, ProcessLogging.LogLevel.ERROR, "Errored at .".concat(e.getLocalizedMessage()), start, new Timestamp(System.currentTimeMillis()));
        }
    }

    public void processConfig(DataSourceConfig config) {
        String configFileUrl = config.getCrawlConfigURL();
        if (configFileUrl == null) {
            loggingService.logProcess(tracker, ProcessLogging.LogLevel.ERROR, String.format("Data source config file URL was null for \"%s\"", config.getName()));
            return;
        }
        File file = new File(configFileUrl);
        if (!file.exists()) {
            loggingService.logProcess(tracker, ProcessLogging.LogLevel.ERROR, String.format("The config file was not found \"%s\"", config.getName()));
            return;
        }
        try {
            JSONObject configJson = new JSONObject(new JSONTokener(new FileReader(configFileUrl)));
            JSONArray array = fetchDataFromJSON(configJson);
            Map<String, Object> maps = configJson.getJSONObject("mapping").toMap();
            List<StagingHeadPhone> list = processResponseData(array, maps);
            this.stagingHeadPhoneRepository.saveAll(list);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private JSONArray fetchDataFromJSON(JSONObject configJson) {
        String url = configJson.getString("url");
        String method = configJson.getString("method");
        JSONObject header = configJson.getJSONObject("header");
        JSONObject body = configJson.getJSONObject("body");

        HttpRequest.Builder builder = httpRequestBuilder;
        builder = builder.uri(URI.create(url));
        for (String headerProp: header.keySet())
            builder = builder.header(headerProp, header.getString(headerProp));
        builder = builder.method(method, HttpRequest.BodyPublishers.ofString(body.toString()));

        HttpRequest request = builder.build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JSONObject result = new JSONObject(response.body());
            return result.getJSONObject("data").getJSONArray("products");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private List<StagingHeadPhone> processResponseData(JSONArray products, Map<String, Object> keyMap) {
        List<StagingHeadPhone> productList = new ArrayList<>();
        for (int i = 0; i < products.length(); i++) {
            JSONObject productObj = products.getJSONObject(i);
            StagingHeadPhone product = new StagingHeadPhone();

            product.setProductId(getJSONCustom(productObj, keyMap.get("product_id").toString()));
            product.setName(getJSONCustom(productObj, keyMap.get("name").toString()));
            product.setBrand(getJSONCustom(productObj, keyMap.get("brand").toString()));
            product.setType(getJSONCustom(productObj, keyMap.get("type").toString()));
            product.setPrice(getJSONCustom(productObj, keyMap.get("price").toString()));
            product.setWarrantyInfo(getJSONCustom(productObj, keyMap.get("warranty_info").toString()));
            product.setFeature(getJSONCustom(productObj, keyMap.get("feature").toString()));
            product.setVoiceControl(getJSONCustom(productObj, keyMap.get("voice_control").toString()));
            product.setMicrophone(getJSONCustom(productObj, keyMap.get("microphone").toString()));
            product.setBatteryLife(getJSONCustom(productObj, keyMap.get("battery_life").toString()));
            product.setDimensions(getJSONCustom(productObj, keyMap.get("dimensions").toString()));
            product.setWeight(getJSONCustom(productObj, keyMap.get("weight").toString()));
            product.setCompatibility(getJSONCustom(productObj, keyMap.get("compatibility").toString()));
            product.setCreatedAt(String.valueOf(LocalDateTime.now()));


//            JSONObject productJson = products.getJSONObject(i);
//            JSONObject general = productJson.getJSONObject("general");
//            JSONObject attributes = general.getJSONObject("attributes");
//            JSONObject filterable = productJson.optJSONObject("filterable");
//
//            StagingHeadPhone product = new StagingHeadPhone();
//            product.setProductId(general.optString("product_id"));
//            product.setName(general.optString("name"));
//            product.setBrand(attributes.optString("phone_accessory_brands"));
//            product.setType(attributes.optString("mobile_accessory_type"));
//            product.setPrice(filterable != null ? filterable.optString("price") : "");
//            product.setWarrantyInfo(attributes.optString("warranty_information"));
//            product.setFeature(attributes.optString("tai_nghe_tinh_nang"));
//            product.setVoiceControl(attributes.optString("tai_nghe_dieu_khien"));
//            product.setMicrophone(attributes.optString("tai_nghe_micro"));
//            product.setBatteryLife(attributes.optString("tai_nghe_pin"));
//            product.setDimensions(attributes.optString("dimensions", attributes.optString("tai_nghe_kich_thuoc_driver", attributes.optString("tai_nghe_do_dai_day", ""))));
//            product.setWeight(attributes.optString("product_weight"));
//            product.setCompatibility(attributes.optString("tai_nghe_tuong_thich"));
//            product.setCreatedAt(String.valueOf(LocalDateTime.now()));

            productList.add(product);
        }

        return productList;
    }

    private String getJSONCustom(JSONObject productObj, String path) {
        String[] split = path.split("\\|");
        String result = null;
        for (String s : split) {
            result = getJSONRecursive(productObj, s);
            if (result != null)
                return result;
        }
        return null;
    }

    private String getJSONRecursive(JSONObject productJson, String path) {
        String[] split = path.split("\\.");
        if (split.length == 0)
            return null;
        if (split.length == 1) {
            return productJson.optString(path, null);
        }
        JSONObject jsonObject = productJson.getJSONObject(split[0]);
        return getJSONRecursive(jsonObject, path.substring(split[0].length() + 1));
    }
}
