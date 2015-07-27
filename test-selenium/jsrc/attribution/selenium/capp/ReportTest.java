
package attribution.selenium.capp;

import java.util.List;

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

public class ReportTest {

    WebDriver driver_;
    WebDriverHelper helper_;

    public ReportTest () {
        System.setProperty("webdriver.chrome.driver", "C:/home/aad/workspace/aad-projects/test-selenium/bin/chromedriver.exe");
        driver_ = new ChromeDriver();
        //driver_ = new FirefoxDriver();

        helper_ = new WebDriverHelper(driver_);
    }

    public void run (String client, String campaign, String subCampaign) {
        login();
        helper_.waitForReportLoadingStart(15, true);
        helper_.waitForReportLoadingDone(60, true);

        selectJob(client, campaign, subCampaign);

        //selectReport("Path Analysis", true);
        selectReport("Player Attribution", false);

        //checkAttributionSummary();

System.err.println("DONE");
        helper_.sleepSeconds(10);
        // done
        logout();
    }

    public void checkAttributionSummary () {
        selectReport("Attribution Summary", true);
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
        if (allConverters != 8870L) {
            throw new RuntimeException("checkAttributionReport: incorrect all converters value.");
        }
        // check attributed converters
        WebElement attributedConvertersElement = table.findElement(By.xpath(".//tbody/tr/td[1]"));
        if (attributedConvertersElement == null) {
            throw new NoSuchElementException("checkAttributionReport: cant't find attributedConverters element.");
        }
        String attributedConvertersString = helper_.getWebElementText(attributedConvertersElement).trim().replaceAll(",", "");
        if (!attributedConvertersString.equalsIgnoreCase("NaN") && !attributedConvertersString.isEmpty()) {
            throw new RuntimeException("checkAttributionReport: incorrect attributed converters value.");
        }
        // check baseline converters
        WebElement baselineConvertersElement = table.findElement(By.xpath(".//tbody/tr/td[2]"));
        if (baselineConvertersElement == null) {
            throw new NoSuchElementException("checkAttributionReport: cant't find baselineConverters element.");
        }
        String baselineConvertersString = helper_.getWebElementText(baselineConvertersElement).trim().replaceAll(",", "");
        long baselineConverters = Long.parseLong(baselineConvertersString);
        if (baselineConverters != 4344L) {
            throw new RuntimeException("checkAttributionReport: incorrect baseline converters value.");
        }
    }

    private void login () {
        WebElement e;
        driver_.get("https://localhost:8043");
        System.out.println("Title: " + driver_.getTitle());
        // login into abakus
        e  = driver_.findElement(By.id("user"));
        e.sendKeys("alexander.dudarenko@abakus.me");
        e  = driver_.findElement(By.id("password"));
        e.sendKeys("is7edGien");
        e  = driver_.findElement(By.id("loginBtn"));
        e.click();
    }

    private void logout () {
        WebElement e;
        e = helper_.waitForElement(By.cssSelector("a[href=\"/j_acegi_logout\"]"), 20);
        e.click();
        driver_.close();
        //driver_.quit();
    }

    private void selectJob (String client, String campaign, String subCampaign) {
        selectClient(client, false);
        selectCampaign(campaign, false);
        selectSubCampaign(subCampaign, false);
    }

    private void selectClient (String clientName, boolean waitloading) {
        WebElement e;
        e = helper_.waitForElement(By.cssSelector("li#profile > a.dropdown-toggle"), 20);
        e.click();
        helper_.sleepSeconds(2);
        e = helper_.waitForElement(By.cssSelector("input[placeholder=\"Search for Client...\"]"), 20);
        helper_.sleepSeconds(3);
        boolean clientWasFound = false;
        for (WebElement div : driver_.findElements(By.cssSelector("div.client-columns div.clientList"))) {
            try {
                WebElement span = div.findElement(By.cssSelector("span.ng-binding"));
                if (clientName.equals(span.getText())) {
                    WebElement radio = div.findElement(By.cssSelector("input[type=\"radio\"][name=\"clientId\"]"));
                    radio.click();
                    clientWasFound = true;
                    break;
                }
            } catch (NoSuchElementException ex) {}
        }
        if (!clientWasFound) {
            throw new NoSuchElementException ("Client '" + clientName + "' was not found.");
        }
        e = driver_.findElement(By.cssSelector("ul.dropdown-menu a[ng-click=\"saveSelectedClient()\"]"));
        e.click();
        e = helper_.waitForElementByText(By.cssSelector("span#clientName"), 30, clientName);
        if (waitloading) {
            helper_.waitForReportLoadingStart(15, false);
            helper_.waitForReportLoadingDone(60, true);
        }
    }


    private void selectCampaign (String campaignName, boolean waitloading) {
        WebElement e;
System.err.println("CHECK FOR CAMAIGN: " + campaignName);
        //e = helper_.waitForElementByText(By.cssSelector("ul#abakus-campaigns-dropdown a.tip.ng-binding"), 100, campaignName);
        e = helper_.waitForElement(By.cssSelector("ul#abakus-campaigns-dropdown a[data-original-title=\""+campaignName+"\"]"), 100);
System.err.println("DONE CEHCK FOR CAMAIGN: " + campaignName);
        e = helper_.waitForElement(By.cssSelector("a#abakus-active-campaign"), 10);
        e.click();
        helper_.sleepSeconds(2);
System.err.println("CLICK");
        e = helper_.waitForElement(By.cssSelector("ul#abakus-campaigns-dropdown a[data-original-title=\""+campaignName+"\"]"), 100);
        e.click();
        e = helper_.waitForElementByText(By.cssSelector("a#abakus-active-campaign"), 20, campaignName);
        if (waitloading) {
            helper_.waitForReportLoadingStart(15, false);
            helper_.waitForReportLoadingDone(60, true);
        }
    }

    private void selectSubCampaign (String subCampaignName, boolean waitloading) {
        WebElement e;
System.err.println("CHECK FOR SUB-CAMPAIGN: " + subCampaignName);
        //e = helper_.waitForElementByText(By.cssSelector("ul#abakus-campaigns-dropdown a.tip.ng-binding"), 100, campaignName);
        e = helper_.waitForElement(By.cssSelector("ul#abakus-subcampaigns-dropdown a[data-original-title=\""+subCampaignName+"\"]"), 100);
System.err.println("DONE CEHCK FOR SUB-CAMPAIGN: " + subCampaignName);
        e = helper_.waitForElement(By.cssSelector("a#abakus-cur-subcampaign"), 10);
        e.click();
        helper_.sleepSeconds(2);
        e = helper_.waitForElement(By.cssSelector("ul#abakus-subcampaigns-dropdown a[data-original-title=\""+subCampaignName+"\"]"), 100);
        e.click();
        e = helper_.waitForElementByText(By.cssSelector("a#abakus-cur-subcampaign"), 20, subCampaignName);
        if (waitloading) {
            helper_.waitForReportLoadingStart(15, false);
            helper_.waitForReportLoadingDone(60, true);
        }
    }

    private void selectReport (String reportName,  boolean waitLoading) {
        reportName = reportName.trim();
        WebElement e;
System.err.println("CHECK FOR REPORT: " + reportName);
        e = helper_.waitForElementByText(By.cssSelector("nav#mainnav li.hasSub.ng-scope span.txt"), 100, reportName);
System.err.println("DONE CEHCK FOR REPORT: " + reportName);
        WebElement menuGroup = null;
        WebElement menuItem = null;
        for (WebElement li : driver_.findElements(By.cssSelector("nav#mainnav li.hasSub.ng-scope"))) {
            for (WebElement span : li.findElements(By.cssSelector("span.txt"))) {
                if (reportName.equalsIgnoreCase(helper_.getWebElementText(span).trim())) {
                    menuGroup = li;
                    menuItem = span;
                }
            }
        }
        if (menuItem == null) {
            throw new NoSuchElementException("selectReport: Can't find report \"" + reportName + "\"");
        }
System.err.println ("REPORT WAS FOUND: " + menuItem);
        e = menuGroup.findElement(By.cssSelector("ul.sub"));
        String menuGroupStyle = e.getAttribute("style");
        if (menuGroupStyle.matches(".*overflow:\\s*hidden.*")) {
System.err.println ("  OPEN MENU: ");
            WebElement a = menuGroup.findElement(By.cssSelector("a[ng-click*=\"toggleIsHidden\"]"));
System.err.println ("          A: " + a);
            a.click();
            helper_.sleepSeconds(2);
        }
        menuItem.click();
        if (waitLoading) {
            helper_.waitForReportLoadingStart(15, false);
            helper_.waitForReportLoadingDone(60, true);
        }
    }

    private void selectCampaign_OLD (String campaignName) {
        WebElement e;
        e = helper_.waitForElement(By.cssSelector("a#abakus-active-campaign"), 10);
        e.click();
        helper_.sleepSeconds(2);
        e = helper_.waitForElementByText(By.cssSelector("ul#abakus-campaigns-dropdown a.tip.ng-binding"), 60, campaignName);
        e.click();
        helper_.sleepSeconds(2);
        e = helper_.waitForElementByText(By.cssSelector("a#abakus-active-campaign"), 20, campaignName);
        helper_.waitForReportLoadingStart(20, false);
        helper_.waitForReportLoadingDone(30, true);
    }


    private void selectSubCampaign_OLD (String subCampaignName) {
        WebElement e;
        e = helper_.waitForElement(By.cssSelector("a#abakus-cur-subcampaign"), 10);
        e.click();
        helper_.sleepSeconds(2);
        e = helper_.waitForElementByText(By.cssSelector("ul#abakus-subcampaigns a.tip.ng-binding"), 60, subCampaignName);
        e.click();
        helper_.sleepSeconds(2);
        e = helper_.waitForElementByText(By.cssSelector("a#bakus-cur-subcampaign"), 20, subCampaignName);
        helper_.waitForReportLoadingStart(30, false);
        helper_.waitForReportLoadingDone(30, true);
    }

    public static void main(String[] args) {
        ReportTest rt = new ReportTest ();
        System.out.println("Start");
        //rt.run("Avis", "NL", "Aug25-Sep21_NL");
        rt.run("Avis", "UK", "May05-May18_UK");
        System.out.println("Done");
    }

}

