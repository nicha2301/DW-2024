package com.nicha.etl;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Huh {
    public static void main(String[] args) throws IOException, JSONException, InterruptedException {
//        String main_url = "https://cellphones.com.vn/thiet-bi-am-thanh/tai-nghe.html";
//
//        EdgeDriver webDriver = new EdgeDriver();
//        WebDriverWait driverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
//        webDriver.get(main_url);
//        WebElement moreElement;
//        while (true) {
//            try {
//                moreElement = driverWait.until(ExpectedConditions.elementToBeClickable(new By.ByCssSelector("#layout-desktop > div.cps-container.cps-body > div:nth-child(2) > div > div.block-filter-sort > div.filter-sort__list-product > div > div.cps-block-content_btn-showmore > a")));
//                webDriver.executeScript("arguments[0].click()", moreElement);
//            }
//            catch (Exception e) {
//                e.printStackTrace(System.out);
//                break;
//            }
//        }
//        System.out.println("done?");
//        List<WebElement> list = webDriver.findElements(new By.ByCssSelector("#layout-desktop > div.cps-container.cps-body > div:nth-child(2) > div > div.block-filter-sort > div.filter-sort__list-product > div > div.product-list-filter.is-flex.is-flex-wrap-wrap > div > div.product-info > a"));
//        TreeSet<String> urls = new TreeSet<>();
//
//        TreeSet<String> trueUrls = new TreeSet<>();
//
//        for (WebElement element: list) {
//            urls.add(element.getAttribute("href"));
//            trueUrls.add(element.getAttribute("href"));
//        }
//        System.out.println(urls.size());
//        for (String url: urls.stream().toList().subList(0, 10)) {
//            webDriver.get(url);
//            driverWait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
//            List<WebElement> list2 = webDriver.findElements(new By.ByCssSelector("#productDetailV2 > section > div.box-detail-product.columns.m-0 > div.box-detail-product__box-center.column > div.box-linked > div > a.item-linked.button__link.linked-0.false"));
//            for (WebElement element: list2) {
//                trueUrls.add(element.getAttribute("href"));
//            }
//        }
//        webDriver.quit();
//        System.out.println(urls.size());
    }
}
