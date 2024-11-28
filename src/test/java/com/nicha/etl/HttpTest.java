package com.nicha.etl;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        JSONObject data = (JSONObject) JSONValue.parse(new FileReader("crawl_conf/cellphones.json"));
        System.out.println(data.toJSONString());
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder = builder.uri(URI.create(data.getAsString("url")));
        JSONObject headers = (JSONObject) data.get("header");
        for (String headerKey : headers.keySet()) {
            builder = builder.header(headerKey, headers.getAsString(headerKey));
        }
        String body = data.getAsString("body");
        builder = builder.method(data.getAsString("method"), HttpRequest.BodyPublishers.ofString(body));

        HttpRequest request = builder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject result = (JSONObject) JSONValue.parse(response.body());
//        JSONObject result_data = (JSONObject) result.get("data");
        JSONArray result_products = (JSONArray) result.get("data.products");

        FileWriter out = new FileWriter("crawl_conf/cellphones_result.json");
        result_products.writeJSONString(out, JSONStyle.NO_COMPRESS);
        out.flush();
        out.close();

    }
}
