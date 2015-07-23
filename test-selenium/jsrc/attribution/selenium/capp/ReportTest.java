
package attribution.selenium.capp;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
        //System.setProperty("webdriver.chrome.driver", "C:/home/aad/workspace/aad-projects/test-selenium/bin/chromedriver.exe");
        //driver_ = new ChromeDriver();
        driver_ = new FirefoxDriver();
        helper_ = new WebDriverHelper(driver_);
    }

    public void run () {
        WebElement e;
        login();
        helper_.waitForReportLoadingStart(30, true);
        helper_.waitForReportLoadingDone(60, true);
        selectClient("Avis");
        selectCampaign("NL");
        selectSubCampaign("Aug25-Sep21_NL");
        helper_.waitForReportLoadingDone(30, false);
System.err.println("DONE");
        helper_.sleepSeconds(10);
        // done
        logout();
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
        driver_.quit();
    }

    private void selectClient (String clientName) {
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
        helper_.waitForReportLoadingStart(20, true);
        helper_.waitForReportLoadingDone(30, true);
    }

    private void selectCampaign (String campaignName) {
        WebElement e;
System.err.println("CHECK FOR CAMAIGN: " + campaignName);
        //e = helper_.waitForElementByText(By.cssSelector("ul#abakus-campaigns-dropdown a.tip.ng-binding"), 100, campaignName);
        e = helper_.waitForElement(By.cssSelector("ul#abakus-campaigns-dropdown a[data-original-title=\""+campaignName+"\"]"), 100);
System.err.println("DONE CEHCK FOR CAMAIGN: " + campaignName);
        e = helper_.waitForElement(By.cssSelector("a#abakus-active-campaign"), 10);
        e.click();
        helper_.sleepSeconds(2);
        e = helper_.waitForElementByText(By.cssSelector("ul#abakus-campaigns-dropdown a.tip.ng-binding"), 60, campaignName);
        e.click();
        helper_.sleepSeconds(2);
        e = helper_.waitForElementByText(By.cssSelector("a#abakus-active-campaign"), 20, campaignName);
        //helper_.waitForReportLoadingStart(20, false);
        //helper_.waitForReportLoadingDone(30, true);
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

    private void selectSubCampaign (String subCampaignName) {
        WebElement e;
System.err.println("CHECK FOR SUB-CAMPAIGN: " + subCampaignName);
        //e = helper_.waitForElementByText(By.cssSelector("ul#abakus-campaigns-dropdown a.tip.ng-binding"), 100, campaignName);
        e = helper_.waitForElement(By.cssSelector("ul#abakus-subcampaigns-dropdown a[data-original-title=\""+subCampaignName+"\"]"), 100);
System.err.println("DONE CEHCK FOR SUB-CAMPAIGN: " + subCampaignName);

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
        rt.run();
        System.out.println("Done");
    }

}

