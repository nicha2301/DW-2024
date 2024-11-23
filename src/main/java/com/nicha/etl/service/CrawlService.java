package com.nicha.etl.service;

import com.nicha.etl.entity.Product;
import com.nicha.etl.entity.StagingHeadPhone;
import com.nicha.etl.repository.ProductRepository;
import com.nicha.etl.repository.StagingHeadPhoneRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CrawlService {

    @Value("${etl.api-url}")
    private String apiUrl;

    @Value("${etl.query}")
    private String query;

    private final LoggingService loggingService;
    private final StagingHeadPhoneRepository stagingHeadPhoneRepository;

    @Autowired
    public CrawlService(LoggingService loggingService, StagingHeadPhoneRepository stagingHeadPhoneRepository) {
        this.loggingService = loggingService;
        this.stagingHeadPhoneRepository = stagingHeadPhoneRepository;
    }

    public void crawlDataAndSaveToStaging() {
        loggingService.logProcess("Crawl Data", "Starting data crawl from API: " + apiUrl, "IN_PROGRESS");
        HttpClient client = HttpClient.newHttpClient();

        // Create JSON payload with the query
        JSONObject requestBody = new JSONObject();
        requestBody.put("query", query);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            // Process and save data to staging
            List<StagingHeadPhone> products = processResponseData(responseBody);
            stagingHeadPhoneRepository.saveAll(products);

            loggingService.logProcess("Crawl Data", "Successfully fetched data from API", "SUCCESS");
        } catch (Exception e) {
            loggingService.logProcess("Crawl Data", "Error fetching data from API: " + e.getMessage(), "ERROR");
        }
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
            product.setDimensions(attributes.optString("dimensions"));
            product.setWeight(attributes.optString("product_weight"));
            product.setCompatibility(attributes.optString("tai_nghe_tuong_thich"));
            product.setCreatedAt(String.valueOf(LocalDateTime.now()));

            productList.add(product);
        }

        return productList;
    }

}
