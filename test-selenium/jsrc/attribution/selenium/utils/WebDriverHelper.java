package attribution.selenium.utils;

import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

/**
 * Copyright (c) 2013-2014 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 7/23/2015
 */
public class WebDriverHelper {

    private static final Logger LOGGER = Logger.getLogger(WebDriverHelper.class);

    private WebDriver driver_;

    public WebDriverHelper(WebDriver driver) {
        this.driver_ = driver;
    }

    public void login (String loginFormUrl, String user, String password) {
        WebElement e;
        driver_.get(loginFormUrl);
        LOGGER.debug("Title: " + driver_.getTitle());
        // login into abakus
        e  = driver_.findElement(By.id("user"));
        e.sendKeys(user);
        e  = driver_.findElement(By.id("password"));
        e.sendKeys(password);
        e  = driver_.findElement(By.id("loginBtn"));
        e.click();
    }

    public void logout () {
        WebElement e;
        e = waitForElement(By.cssSelector("a[href=\"/j_acegi_logout\"]"), 20);
        e.click();
        driver_.close();
        //driver_.quit();
    }

    public void selectJob (String client, String campaign, String subCampaign) {
        selectClient(client, false);
        selectCampaign(campaign, false);
        selectSubCampaign(subCampaign, true);
    }

    public void selectClient (String clientName, boolean waitloading) {
        WebElement e;
        e = waitForElement(By.cssSelector("li#profile > a.dropdown-toggle"), 20);
        e.click();
        sleepSeconds(1);
        e = waitForElement(By.cssSelector("input[placeholder=\"Search for Client...\"]"), 20);
        sleepSeconds(2);
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
        e = waitForElementByText(By.cssSelector("span#clientName"), 30, clientName);
        if (waitloading) {
            waitForReportLoadingStart(15, false);
            waitForReportLoadingDone(60, true);
        }
    }


    public void selectCampaign (String campaignName, boolean waitloading) {
        WebElement e;
        LOGGER.debug("CHECK FOR CAMAIGN: " + campaignName);
        //e = waitForElementByText(By.cssSelector("ul#abakus-campaigns-dropdown a.tip.ng-binding"), 100, campaignName);
        e = waitForElement(By.cssSelector("ul#abakus-campaigns-dropdown a[data-original-title=\""+campaignName+"\"]"), 100);
        LOGGER.debug("DONE CEHCK FOR CAMAIGN: " + campaignName);
        e = waitForElement(By.cssSelector("a#abakus-active-campaign"), 10);
        e.click();
        sleepSeconds(2);
        LOGGER.debug("CLICK");
        e = waitForElement(By.cssSelector("ul#abakus-campaigns-dropdown a[data-original-title=\""+campaignName+"\"]"), 100);
        e.click();
        e = waitForElementByText(By.cssSelector("a#abakus-active-campaign"), 20, campaignName);
        if (waitloading) {
            waitForReportLoadingStart(15, false);
            waitForReportLoadingDone(120, true);
        }
    }

    public void selectSubCampaign (String subCampaignName, boolean waitloading) {
        WebElement e;
        LOGGER.debug("CHECK FOR SUB-CAMPAIGN: " + subCampaignName);
        //e = waitForElementByText(By.cssSelector("ul#abakus-campaigns-dropdown a.tip.ng-binding"), 100, campaignName);
        e = waitForElement(By.cssSelector("ul#abakus-subcampaigns-dropdown a[data-original-title=\""+subCampaignName+"\"]"), 100);
        LOGGER.debug("DONE CEHCK FOR SUB-CAMPAIGN: " + subCampaignName);
        e = waitForElement(By.cssSelector("a#abakus-cur-subcampaign"), 10);
        e.click();
        sleepSeconds(2);
        e = waitForElement(By.cssSelector("ul#abakus-subcampaigns-dropdown a[data-original-title=\""+subCampaignName+"\"]"), 100);
        e.click();
        e = waitForElementByText(By.cssSelector("a#abakus-cur-subcampaign"), 20, subCampaignName);
        if (waitloading) {
            waitForReportLoadingStart(15, false);
            waitForReportLoadingDone(120, true);
        }
    }

    public void selectReport (String reportName,  boolean waitLoading) {
        reportName = reportName.trim();
        WebElement e;
        LOGGER.debug("CHECK FOR REPORT: " + reportName);
        e = waitForElementByText(By.cssSelector("nav#mainnav li.hasSub.ng-scope span.txt"), 100, reportName);
        LOGGER.debug("DONE CEHCK FOR REPORT: " + reportName);
        WebElement menuGroup = null;
        WebElement menuItem = null;
        LOOP: for (WebElement li : driver_.findElements(By.cssSelector("nav#mainnav li.hasSub.ng-scope"))) {
            for (WebElement span : li.findElements(By.cssSelector("span.txt"))) {
                if (reportName.equalsIgnoreCase(getWebElementText(span).trim())) {
                    menuGroup = li;
                    menuItem = span;
                    break LOOP;
                }
            }
        }
        if (menuItem == null) {
            throw new NoSuchElementException("selectReport: Can't find report \"" + reportName + "\"");
        }
        LOGGER.debug("REPORT WAS FOUND: " + menuItem);
        e = menuGroup.findElement(By.cssSelector("ul.sub"));
        String menuGroupStyle = e.getAttribute("style");
        if (menuGroupStyle.matches(".*overflow:\\s*hidden.*")) {
            LOGGER.debug("OPEN MENU: ");
            WebElement a = menuGroup.findElement(By.cssSelector("a[ng-click*=\"toggleIsHidden\"]"));
            LOGGER.debug("   A: " + a);
            a.click();
            sleepSeconds(2);
        }
        menuItem.click();
        sleepSeconds(5);
        if (waitLoading) {
            waitForReportLoadingStart(15, false);
            waitForReportLoadingDone(120, true);
        }
    }

    public boolean isElementPresent(By by) {
        try {
          driver_.findElement(by);
          return true;
        } catch (NoSuchElementException e) {
          return false;
        }
    }

    public WebElement waitForElement(By by, int seconds) {
        WebElement we = null;
        do {
            LOGGER.debug("wait: <" +by+ ">");
            try {
              we = driver_.findElement(by);
            LOGGER.debug("   OK");
              break;
            } catch (NoSuchElementException e) {}
            if (seconds > 0) {
                try {Thread.sleep(1000L);} catch (InterruptedException ex) {};
                -- seconds;
            }
        } while (seconds > 0);
        if (we == null) {
            throw new NoSuchElementException("waitForElement: Can't find element \"" + by + "\"");
        }
        return we;
    }

    public WebElement waitForElementByText(By by, int seconds, String text) {
        text = text.trim().toUpperCase();
        do {
            try {
                List<WebElement> l = driver_.findElements(by);
                LOGGER.debug("waitByText: size=" +l.size());
                for (WebElement e : l) {
                    String elementText = e.getAttribute("textContent");
                    if (elementText == null || elementText.isEmpty()) {
                        elementText = e.getAttribute("innerHTML");
                    }
                    elementText = elementText.trim();
                    LOGGER.debug("waitByText: <" +by+ "> <" + elementText + ">");
                    if (text.equals(elementText.toUpperCase())) {
                        LOGGER.debug("   OK");
                        return e;
                    }
                }
            } catch (StaleElementReferenceException ex) {
                continue;
            }
            if (seconds > 0) {
                try {Thread.sleep(1000L);} catch (InterruptedException ex) {};
                -- seconds;
            }
        } while (seconds > 0);
        throw new NoSuchElementException("waitForElementByText: Can't find element \"" + by + "\" by text \"" + text + "\"");
    }

    public String getWebElementText(WebElement e) {
        String elementText = e.getAttribute("textContent");
        if (elementText == null || elementText.isEmpty()) {
            elementText = e.getAttribute("innerHTML");
        }
        return elementText;
    }

    public int getWebElementInteger(WebElement e) {
        String text = getWebElementText(e).trim().replaceAll(",", "");
        return Integer.parseInt(text);
    }

    public long getWebElementLong(WebElement e) {
        String text = getWebElementText(e).trim().replaceAll(",", "");
        return Long.parseLong(text);
    }

    public double getWebElementDouble(WebElement e) {
        String text = getWebElementText(e).trim().replaceAll(",", "");
        return Double.parseDouble(text);
    }

    public void waitForReportLoadingStart(int seconds, boolean throwException) {
        //By by = By.cssSelector("div.progress-striped.progress:visible");
        By by = By.cssSelector("div.progress-striped.progress");
        boolean ok = false;
        LOOP: do {
            try {
                List<WebElement> l = driver_.findElements(by);
                for (WebElement e : l) {
                    String style = e.getAttribute("style");
                    LOGGER.debug("loadingStart: e=<"+e+"> style=" + style);
                    if (style == null || !style.matches(".*display:\\s*none.*")) {
                        LOGGER.debug("     OK: ");
                        ok = true;
                        break LOOP;
                    }
                }
            } catch (StaleElementReferenceException ex) {
                continue;
            }
            if (seconds > 0) {
                try {Thread.sleep(1000L);} catch (InterruptedException ex) {};
                -- seconds;
            }
        } while (seconds > 0);
        if (throwException && !ok) {
            throw new WebDriverException("waitForReportLoadingStart: timeout was reached.");
        }
    }

    public void waitForReportLoadingDone(int seconds, boolean throwException) {
        By by = By.cssSelector("div.progress-striped.progress");
        boolean ok = false;
        do {
            boolean visible = false;
            try {
                List<WebElement> l = driver_.findElements(by);
                for (WebElement e : l) {
                    String style = e.getAttribute("style");
                    if (style == null || !style.matches(".*display:\\s*none.*")) {
                        visible = true;
                        break;
                    }
                }
            } catch (StaleElementReferenceException ex) {
                continue;
            }
            if (!visible) {
                ok = true;
                break;
            }
            if (seconds > 0) {
                try {Thread.sleep(1000L);} catch (InterruptedException ex) {};
                -- seconds;
            }
        } while (seconds > 0);
        if (throwException && !ok) {
            throw new WebDriverException("waitForReportLoadingDone: timeout was reached.");
        }
    }

    public void sleepSeconds (int seconds) {
        try {Thread.sleep(1000L * seconds);} catch (InterruptedException ex) {};
    }

}
