package com.nicha.etl.service;

import com.nicha.etl.entity.config.DataSourceConfig;
import com.nicha.etl.entity.config.ProcessLogging;
import com.nicha.etl.repository.config.DataSourceConfigRepository;
import com.nicha.etl.repository.config.ProcessTrackerRepository;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CrawlService extends AbstractEtlService {

    private final DataSourceConfigRepository dataSourceConfigRepository;
    private final HttpClient httpClient;
    private final HttpRequest.Builder httpRequestBuilder;

    protected CrawlService(LoggingService loggingService,
                           ProcessTrackerRepository trackerRepo,
                           DataSourceConfigRepository dataSourceConfigRepository) {
        super(loggingService, trackerRepo, "Crawl Data");
        this.dataSourceConfigRepository = dataSourceConfigRepository;
        this.httpClient = HttpClient.newBuilder().build();
        this.httpRequestBuilder = HttpRequest.newBuilder();
    }

    @Override
    protected void process(boolean forceRun) {
        logProcess(ProcessLogging.LogLevel.DEBUG, "Getting all data source config to crawl.");
        List<DataSourceConfig> configs = this.dataSourceConfigRepository.findAll();

        for (DataSourceConfig dataSourceConfig : configs) {
            logProcess(ProcessLogging.LogLevel.DEBUG, "Working with data source: " + dataSourceConfig.getName());
            crawlAndExportCSV(dataSourceConfig);
            logProcess(ProcessLogging.LogLevel.DEBUG, "Completed crawling with data source: " + dataSourceConfig.getName());
        }
    }

    private void crawlAndExportCSV(DataSourceConfig config) {
        String configFileUrl = config.getCrawlConfigURL();
        if (configFileUrl == null)
            throw new RuntimeException(String.format("Data source config file URL was null for \"%s\"", config.getName()));

        File file = new File(configFileUrl);
        if (!file.exists())
            throw new RuntimeException(String.format("The config file was not found \"%s\"", config.getName()));


        String saveLocationURL = config.getCrawlSaveLocation();
        if (saveLocationURL == null)
            throw new RuntimeException(String.format("Data source save location URL was null for \"%s\"", config.getName()));

        try {
            // Read config json
            JSONObject configJson = new JSONObject(new JSONTokener(new FileReader(configFileUrl)));
            // Try fetching the source
            JSONObject object = fetchDataHttps(configJson);
            // Get the products arrays
            JSONArray array = getJSONRecursiveArray(object, configJson.getString("location"));
            // Split the fields from config
            String[] fields = config.getStagingFields().split(",");
            // Get config mappings for data
            Map<String, Object> maps = configJson.getJSONObject("mapping").toMap();
            // Now do it
            List<Map<String, String>> list = processResponseData(array, fields, maps);
            List<String[]> entries = new ArrayList<>();
            String[] uhh;
            entries.add(fields);
            for (Map<String, String> entry : list) {
                uhh = new String[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    uhh[i] = entry.getOrDefault(fields[i], "");
                }
                entries.add(uhh);
            }

            Calendar c1 = Calendar.getInstance();
            c1.setTime(tracker.getStartTime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy_hhmm");
            saveLocationURL = saveLocationURL.replace("ddmmyy_hhmm", dateFormat.format(c1.getTime()));
            exportToCSV(entries, saveLocationURL);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private JSONObject fetchDataHttps(JSONObject configJson) {
        String url = configJson.getString("url");
        String method = configJson.getString("method");
        JSONObject header = configJson.getJSONObject("header");
        JSONObject body = configJson.getJSONObject("body");

        HttpRequest.Builder builder = httpRequestBuilder;
        builder = builder.uri(URI.create(url));
        for (String headerProp : header.keySet())
            builder = builder.header(headerProp, header.getString(headerProp));
        builder = builder.method(method, HttpRequest.BodyPublishers.ofString(body.toString()));

        HttpRequest request = builder.build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new JSONObject(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Map<String, String>> processResponseData(JSONArray products, String[] fields, Map<String, Object> keyMap) {
        List<Map<String, String>> productList = new ArrayList<>();
        for (int i = 0; i < products.length(); i++) {
            JSONObject productObj = products.getJSONObject(i);
            Map<String, String> productMap = new HashMap<>();
            for (String field : fields) {
                productMap.put(field, getJSONCustom(productObj, keyMap.getOrDefault(field, "").toString()));
            }
            productList.add(productMap);
        }
        return productList;
    }

    // These methods are for retrieving data recursively
    private String getJSONCustom(JSONObject productObj, String path) {
        String[] split = path.split("\\|");
        String result;
        for (String s : split) {
            result = getJSONRecursiveString(productObj, s);
            if (result != null)
                return result;
        }
        return null;
    }

    private String getJSONRecursiveString(JSONObject productJson, String path) {
        String[] split = path.split("\\.");
        if (split.length == 0)
            return null;
        if (split.length == 1) {
            return productJson.optString(path, null);
        }
        JSONObject jsonObject = productJson.getJSONObject(split[0]);
        return getJSONRecursiveString(jsonObject, path.substring(split[0].length() + 1));
    }

    private JSONArray getJSONRecursiveArray(JSONObject productJson, String path) {
        String[] split = path.split("\\.");
        if (split.length == 0)
            return null;
        if (split.length == 1) {
            return productJson.optJSONArray(path, null);
        }
        JSONObject jsonObject = productJson.getJSONObject(split[0]);
        return getJSONRecursiveArray(jsonObject, path.substring(split[0].length() + 1));
    }

    private void exportToCSV(List<String[]> data, String exportURL) throws IOException {
        CSVWriterBuilder builder = new CSVWriterBuilder(new FileWriter(exportURL));
        ICSVWriter writer = builder.withSeparator(',').build();
        writer.writeAll(data, true);
        writer.flush();
        writer.close();
    }
}
