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
        List<StagingHeadPhone> products = new ArrayList<>();
        JSONArray data = new JSONArray(responseBody);

        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);

            StagingHeadPhone product = new StagingHeadPhone();
            product.setProductId(jsonObject.optString("id"));
            product.setName(jsonObject.optString("name"));
            product.setBrand(jsonObject.optString("brand"));
            product.setType(jsonObject.optString("type"));
            product.setPrice(jsonObject.optBigDecimal("price", BigDecimal.ZERO));
            product.setWarrantyInfo(jsonObject.optString("warrantyInfo"));
            product.setFeature(jsonObject.optString("feature"));
            product.setVoiceControl(jsonObject.optString("voiceControl"));
            product.setMicrophone(jsonObject.optString("microphone"));
            product.setBatteryLife(jsonObject.optString("batteryLife"));
            product.setDimensions(jsonObject.optString("dimensions"));
            product.setWeight(jsonObject.optString("weight"));
            product.setCompatibility(jsonObject.optString("compatibility"));

            products.add(product);
        }

        return products;
    }

}
