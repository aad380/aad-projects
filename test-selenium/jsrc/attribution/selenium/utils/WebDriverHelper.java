package attribution.selenium.utils;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

/**
 * Copyright (c) 2013-2014 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 7/23/2015
 */
public class WebDriverHelper {

    private WebDriver driver_;

    public WebDriverHelper(WebDriver driver) {
        this.driver_ = driver;
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
System.err.println("wait: <" +by+ ">");
            try {
              we = driver_.findElement(by);
System.err.println("   OK");
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
        text = text.trim();
        do {
            List<WebElement> l = driver_.findElements(by);
            for (WebElement e : l) {
System.err.println("waitByText: <" +by+ "> <" + e.getText() + ">");
                if (text.equals(e.getText().trim())) {
System.err.println("   OK");
                    return e;
                }
            }
            if (seconds > 0) {
                try {Thread.sleep(1000L);} catch (InterruptedException ex) {};
                -- seconds;
            }
        } while (seconds > 0);
        throw new NoSuchElementException("waitForElementByText: Can't find element \"" + by + "\" by text \"" + text + "\"");
    }

    public void waitForReportLoadingStart(int seconds, boolean throwException) {
        //By by = By.cssSelector("div.progress-striped.progress:visible");
        By by = By.cssSelector("div.progress-striped.progress");
        boolean ok = false;
        LOOP: do {
            List<WebElement> l = driver_.findElements(by);
            for (WebElement e : l) {
                String style = e.getAttribute("style");
System.err.println("loadingStart: e=<"+e+"> style=" + style);
                if (style == null || !style.matches(".*display:\\s*none.*")) {
System.err.println("     OK: ");
                    ok = true;
                    break LOOP;
                }
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
            List<WebElement> l = driver_.findElements(by);
            boolean visible = false;
            for (WebElement e : l) {
                String style = e.getAttribute("style");
                if (style == null || !style.matches(".*display:\\s*none.*")) {
                    visible = true;
                    break;
                }
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
