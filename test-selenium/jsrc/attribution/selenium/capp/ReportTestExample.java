
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
    public static String CAPP_PASSWORD = "welcome";

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
        LOGGER.info("LOGIN TO SITE");
        helper_.login(loginFormUrl_, user_, password_);
        helper_.waitForReportLoadingStart(30, false);
        helper_.waitForReportLoadingDone(120, true);
        LOGGER.info("SELECT JOB");
        helper_.selectJob(client, campaign, subCampaign);
        //selectReport("Path Analysis", true);
//        selectReport("Player Attribution", false);
//        selectReport("Attribution Summary", false);

        LOGGER.info("CHECK REPORT: Attribution Summary");
        checkAttributionSummary();
        LOGGER.info("CHECK REPORT: Player Attribution");
        checkPlayerAttribution();
        LOGGER.info("CHECK REPORT: Player Efficiency");
        checkPlayerEfficiency();
        LOGGER.info("DONE");
        helper_.sleepSeconds(30);
        // done
        helper_.logout();
    }

    public void checkAttributionSummary () {
        helper_.selectReport("Attribution Summary", true);
        WebElement table = driver_.findElement(By.cssSelector("table#table.table.dataTable"));
        if (table == null) {
            throw new NoSuchElementException("checkAttributionSummaryReport: cant't find datatable.");
        }
        WebElement e;
        // check all converters
        e = table.findElement(By.xpath(".//thead/tr[2]/th"));
        if (e == null) {
            throw new NoSuchElementException("checkAttributionSummaryReport: cant't find allConverters element.");
        }
        int allConverters = helper_.getWebElementInteger(e);
        if (allConverters != 11219) {
            throw new RuntimeException("checkAttributionSummaryReport: incorrect all converters value.");
        }
        // check attributed converters
        e = table.findElement(By.xpath(".//tbody/tr/td[1]"));
        if (e == null) {
            throw new NoSuchElementException("checkAttributionSummaryReport: cant't find attributedConverters element.");
        }
        int attributedConverters = helper_.getWebElementInteger(e);
        if (attributedConverters != 211) {
            throw new RuntimeException("checkAttributionSummaryReport: incorrect attributed converters value.");
        }
        // check baseline converters
        e = table.findElement(By.xpath(".//tbody/tr/td[2]"));
        if (e == null) {
            throw new NoSuchElementException("checkAttributionSummaryReport: cant't find baselineConverters element.");
        }
        int baselineConverters = helper_.getWebElementInteger(e);
        if (baselineConverters != 11008) {
            throw new RuntimeException("checkAttributionSummaryReport: incorrect baseline converters value.");
        }
        if (allConverters != attributedConverters + baselineConverters) {
            throw new RuntimeException("checkAttributionSummaryReport: allConverters("+allConverters+") != attributedConverters("+attributedConverters+") + baselineConverters("+baselineConverters+")");
        }
    }

    public void checkPlayerAttribution () {
        /* pathes for tables
            xpath: //div[@id='table_wrapper']/div[4]/div/div/table/thead/tr[3]/td[2]
            css: "div#table_wrapper div.dataTables_scrollHeadInner table.dataTable"
            xpath://table[@id='table']/tbody/tr[2]/td[2]
            css: div#table_wrapper div.dataTables_scrollBody table.dataTable
        */
        helper_.selectReport("Player Attribution", true);
        WebElement top_table = helper_.waitForElement(By.cssSelector("div#table_wrapper div.dataTables_scrollHeadInner table.dataTable"), 20);
        if (top_table == null) {
            throw new NoSuchElementException("checkPlayerAttributionReport: cant't find top_table.");
        }
        WebElement bottom_table = helper_.waitForElement(By.cssSelector("div#table_wrapper div.dataTables_scrollBody table.dataTable"), 20);
        if (bottom_table == null) {
            throw new NoSuchElementException("checkPlayerAttributionReport: cant't find bottom_table.");
        }
        WebElement e = top_table.findElement(By.xpath(".//thead/tr[3]/td[2]"));
        if (e == null) {
            throw new NoSuchElementException("checkPlayerAttributionReport: cant't find allPlayersTotal element.");
        }
        double allPlayersTotal = helper_.getWebElementDouble(e);
        if (allPlayersTotal != 211.0) {
            throw new RuntimeException("checkPlayersAttributionReport: incorrect allPlayersTotal value.");
        }
    }

    public void checkPlayerEfficiency () {
        helper_.selectReport("Player Efficiency", true);
    }

    public static void main(String[] args) {
        ReportTestExample rt = new ReportTestExample (CAPP_LOGIN_FORM_URL, CAPP_USER, CAPP_PASSWORD);
        LOGGER.info("START TESTS");
        //rt.run("Avis", "NL", "Aug25-Sep21_NL");
        //rt.run("Avis", "UK", "May05-May18_UK");
        //rt.run("Avis", "UK", "Sep15-Sep28_UK");
        rt.run("Quantcast", "Careers", "Jul 01-Jul 31, 2014");
    }

}

