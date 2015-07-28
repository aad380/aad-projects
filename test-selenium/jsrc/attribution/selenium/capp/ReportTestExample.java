
package attribution.selenium.capp;

import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import attribution.selenium.utils.WebDriverHelper;

/**
 * Copyright (c) 2013-2015 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 7/23/2015
 */

public class ReportTestExample {

    private static final Logger LOGGER = Logger.getLogger(ReportTestExample.class);
    //public static String CAPP_LOGIN_URL = "https://localhost:8043/login";
    public static String CAPP_LOGIN_FORM_URL = "https://ec2-54-67-77-250.us-west-1.compute.amazonaws.com/login";
    public static String CAPP_USER = "alexander.dudarenko@abakus.me";
    public static String CAPP_PASSWORD = "is7edGien";

    private WebDriver driver_;
    private WebDriverHelper helper_;
    
    private String loginFormUrl_;
    private String user_;
    private String password_;

    public ReportTestExample (String loginFormUrl, String user, String password) {
        loginFormUrl_ = loginFormUrl;
        user_ = user;
        password_ = password;
        //System.setProperty("webdriver.chrome.driver", "C:/home/aad/workspace/aad-projects/test-selenium/bin/chromedriver.exe");
        //driver_ = new ChromeDriver();
        driver_ = new FirefoxDriver();
        helper_ = new WebDriverHelper(driver_);
    }

    public void run (String client, String campaign, String subCampaign) {
        helper_.login(loginFormUrl_, user_, password_);
        helper_.waitForReportLoadingStart(30, false);
        helper_.waitForReportLoadingDone(120, true);

        helper_.selectJob(client, campaign, subCampaign);

        //selectReport("Path Analysis", true);
//        selectReport("Player Attribution", false);
//        selectReport("Attribution Summary", false);

        checkAttributionSummary();
        checkPlayerAttribution();
        checkPlayerEfficiency();

System.err.println("DONE");

        helper_.sleepSeconds(30);
        // done
        helper_.logout();
    }

    public void checkAttributionSummary () {
        helper_.selectReport("Attribution Summary", true);
        WebElement table = driver_.findElement(By.cssSelector("table#table.table.dataTable"));
        if (table == null) {
            throw new NoSuchElementException("checkAttributionReport: cant't find datatable.");
        }
        // check all converters
        WebElement allConvertersElement = table.findElement(By.xpath(".//thead/tr[2]/th"));
        if (allConvertersElement == null) {
            throw new NoSuchElementException("checkAttributionReport: cant't find allConverters element.");
        }
        String allConvertersString = helper_.getWebElementText(allConvertersElement).trim().replaceAll(",", "");
        long allConverters = Long.parseLong(allConvertersString);
        if (allConverters != 11219L) {
            throw new RuntimeException("checkAttributionReport: incorrect all converters value.");
        }
        // check attributed converters
        WebElement attributedConvertersElement = table.findElement(By.xpath(".//tbody/tr/td[1]"));
        if (attributedConvertersElement == null) {
            throw new NoSuchElementException("checkAttributionReport: cant't find attributedConverters element.");
        }
        String attributedConvertersString = helper_.getWebElementText(attributedConvertersElement).trim().replaceAll(",", "");
        long attributedConverters = Long.parseLong(attributedConvertersString);
        if (attributedConverters != 211L) {
            throw new RuntimeException("checkAttributionReport: incorrect attributed converters value.");
        }
        // check baseline converters
        WebElement baselineConvertersElement = table.findElement(By.xpath(".//tbody/tr/td[2]"));
        if (baselineConvertersElement == null) {
            throw new NoSuchElementException("checkAttributionReport: cant't find baselineConverters element.");
        }
        String baselineConvertersString = helper_.getWebElementText(baselineConvertersElement).trim().replaceAll(",", "");
        long baselineConverters = Long.parseLong(baselineConvertersString);
        if (baselineConverters != 11008L) {
            throw new RuntimeException("checkAttributionReport: incorrect baseline converters value.");
        }
    }

    public void checkPlayerAttribution () {
        helper_.selectReport("Player Attribution", true);
    }

    public void checkPlayerEfficiency () {
        helper_.selectReport("Player Efficiency", true);
    }

    public static void main(String[] args) {
        ReportTestExample rt = new ReportTestExample (CAPP_LOGIN_FORM_URL, CAPP_USER, CAPP_PASSWORD);
        System.out.println("Start");
        //rt.run("Avis", "NL", "Aug25-Sep21_NL");
        //rt.run("Avis", "UK", "May05-May18_UK");
        //rt.run("Avis", "UK", "Sep15-Sep28_UK");
        rt.run("Quantcast", "Careers", "Jul 01-Jul 31, 2014");
        System.out.println("Done");
    }

}

