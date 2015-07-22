package attribution.selenium.capp;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.thoughtworks.selenium.Selenium;

/**
 * Copyright (c) 2013-2015 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 7/22/2015
 */

public class LoginLogout {
    Selenium selenium;
    public LoginLogout() {
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
        WebElement e;
        //System.setProperty("webdriver.chrome.driver", "C:/home/aad/workspace/aad-projects/test-selenium/bin/chromedriver.exe");
        //WebDriver driver = new ChromeDriver();
        WebDriver driver = new FirefoxDriver();
        driver.get("https://localhost:8043");
        System.out.println("Title: " + driver.getTitle());
        // login into abakus
        e  = driver.findElement(By.id("user"));
        e.sendKeys("alexander.dudarenko@abakus.me");
        e  = driver.findElement(By.id("password"));
        e.sendKeys("is7edGien");
        e  = driver.findElement(By.id("loginBtn"));
        e.click();
        // waith for close button and logout
        e = waitForElement(driver, By.cssSelector("li.ng-scope > a[href=\"/j_acegi_logout\"]"), 10);
        e.click();
        // done
        try {Thread.sleep(10000L);} catch (InterruptedException ex) {};
        driver.close();
        driver.quit();
        System.out.println("Done");
    }

    public static WebElement waitForElement(WebDriver driver, By by, int seconds) {
        WebElement we = null;
        while (seconds >= 0) {
            try {
              we = driver.findElement(by);
              break;
            } catch (NoSuchElementException e) {}
            try {Thread.sleep(1000L);} catch (InterruptedException ex) {};
            -- seconds;
        } 
        if (we == null) {
            throw new NoSuchElementException("Can't find element: " + by);
        }
        return we;
    }

    private static boolean isElementPresent(WebDriver driver, By by) {
        try {
          driver.findElement(by);
          return true;
        } catch (NoSuchElementException e) {
          return false;
        }
      }
}
