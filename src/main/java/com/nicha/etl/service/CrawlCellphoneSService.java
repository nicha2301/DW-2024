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
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

@Service
public class CrawlCellphoneSService extends AbstractEtlService {

    private final DataSourceConfigRepository dataSourceConfigRepository;
    private final HttpClient httpClient;
    private final HttpRequest.Builder httpRequestBuilder;

    @Autowired
    protected CrawlCellphoneSService(LoggingService loggingService,
                                     ProcessTrackerRepository trackerRepo,
                                     DataSourceConfigRepository dataSourceConfigRepository) {
        super(loggingService, trackerRepo, "Crawl CellphoneS Data");
        this.dataSourceConfigRepository = dataSourceConfigRepository;
        this.httpClient = HttpClient.newBuilder().build();
        this.httpRequestBuilder = HttpRequest.newBuilder();
    }

    @Override
    protected void process(boolean forceRun) {
        logProcess(ProcessLogging.LogLevel.DEBUG, "Getting all data source config to crawl.");
        DataSourceConfig config = this.dataSourceConfigRepository.findByName("CellphoneS");

        logProcess(ProcessLogging.LogLevel.DEBUG, "Working with data source: " + config.getName());
        crawlAndExportCSV(config);
        logProcess(ProcessLogging.LogLevel.DEBUG, "Completed crawling with data source: " + config.getName());
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

            List<String[]> entries = new ArrayList<>();
            // Split the fields from config
            String[] fields = "product_id,name,brand,type,price,warranty_info,feature,voice_control,microphone,battery_life,dimensions,weight,compatibility".split(",");
            // Get config mappings for data
            Map<String, Object> maps = configJson.getJSONObject("mapping").toMap();
            List<Map<String, String>> list = new ArrayList<>();
            if (configJson.getString("type").equals("api")) {
                // Try fetching the source
                JSONObject object = fetchDataHttps(configJson);
                // Get the products arrays
                JSONArray array = getJSONRecursiveArray(object, configJson.getString("location"));
                // Now do it
                assert array != null;
                list = processResponseData(array, fields, maps);
            }
            else if (configJson.getString("type").equals("crawl")) {
                list = crawlSite(configJson);
            }

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
            System.out.println(tracker.getStartTime());
            c1.setTime(tracker.getStartTime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy_HHmm");
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

    private List<Map<String, String>> crawlSite(JSONObject configJson) {
        String mainUrl = configJson.getString("url");
        Map<String, Object> maps = configJson.getJSONObject("mapping").toMap();
//        String loadMore = configJson.getString("load_more_btn");

        Set<Map<String, String>> result = new HashSet<>();
        String[] fields = "product_id,name,brand,type,price,warranty_info,feature,voice_control,microphone,battery_life,dimensions,weight,compatibility".split(",");

        EdgeDriver webDriver = new EdgeDriver();
        WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        webDriver.get(mainUrl);
        Map<String, Object> map = configJson.getJSONObject("location").toMap();
        Set<String> urls = findElements(webDriver, map);
        Set<String> urls2 = new TreeSet<>();
//        WebElement loadMoreElement;
//        while (true) {
//            try {
//                loadMoreElement = webDriverWait.until(ExpectedConditions.elementToBeClickable(new By.ByCssSelector(loadMore)));
//                webDriver.executeScript("arguments[0].click();", loadMoreElement);
//            } catch (TimeoutException e) {
//                break;
//            }
//        }

        assert urls != null;
        for (String url: urls) {
            webDriver.get(url);
            webDriverWait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
            urls2 = findElements(webDriver, configJson.getJSONObject("other_location").toMap());
            try {

                Map<String, String> productMap = new HashMap<>();
                for (String field : fields) {
                    HashMap<String, Object> fieldData = (HashMap<String, Object>) maps.get(field);
                    String element = findElement(webDriver, fieldData);
                    productMap.put(field, element);
                }
                System.out.println("1, " + productMap);
                result.add(productMap);
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
        assert urls2 != null;
        for (String url: urls2) {
            webDriver.get(url);
            webDriverWait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
            try {
                Map<String, String> productMap = new HashMap<>();
                for (String field : fields) {
                    HashMap<String, Object> fieldData = (HashMap<String, Object>) maps.get(field);
                    String element = findElement(webDriver, fieldData);
                    productMap.put(field, element);
                }
                System.out.println("2, "  + productMap);
                result.add(productMap);
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
        webDriver.quit();
        return result.stream().toList();
    }

    private Set<String> findElements(WebDriver instance, Map<String, Object> fieldData) {
        Set<String> result = new HashSet<>();
        String by = fieldData.get("by").toString().trim();
        String path = fieldData.get("path").toString().trim();
        String attribute = fieldData.get("attribute").toString().trim();

        By byObj = switch (by) {
            case "id" -> By.id(path);
            case "tag" -> By.tagName(path);
            case "name" -> By.name(path);
            case "class" -> By.className(path);
            case "href" -> By.linkText(path);
            case "css" -> By.cssSelector(path);
            case "xpath" -> By.xpath(path);
            default -> null;
        };
        if (byObj == null) {
            return null;
        }
        List<WebElement> elementList;
        try {
            elementList = instance.findElements(byObj);
            for (WebElement element: elementList) {
                if (!attribute.isEmpty())
                    result.add(element.getAttribute(attribute));
                else
                    result.add(element.getText());
            }
        }
        catch (NoSuchElementException e) {
            e.printStackTrace(System.err);
            result.add("");
        }
        return result;
    }

    private String findElement(WebDriver instance, Map<String, Object> fieldData) {
        String by = fieldData.get("by").toString().trim();
        String path = fieldData.get("path").toString().trim();
        String attribute = fieldData.get("attribute").toString().trim();

        By byObj = switch (by) {
            case "id" -> By.id(path);
            case "tag" -> By.tagName(path);
            case "name" -> By.name(path);
            case "class" -> By.className(path);
            case "href" -> By.linkText(path);
            case "css" -> By.cssSelector(path);
            case "xpath" -> By.xpath(path);
            default -> null;
        };
        if (byObj == null) {
            return null;
        }
        WebElement element;
        try {
            element = instance.findElement(byObj);
            if (!attribute.isEmpty())
                return element.getAttribute(attribute);
            return element.getText();
        }
        catch (NoSuchElementException e) {
            e.printStackTrace(System.err);
            return "";
        }
    }
}
