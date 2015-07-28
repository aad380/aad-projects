package attribution.selenium.capp;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import attribution.selenium.utils.WebDriverHelper;


/**
 * Copyright (c) 2013-2015 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 7/22/2015
 */

public class LoginLogoutTestExample {

    public LoginLogoutTestExample() {
        // TODO Auto-generated constructor stub
    }
    
    public static void main(String[] args) {
        WebElement e;
        //System.setProperty("webdriver.chrome.driver", "C:/home/aad/workspace/aad-projects/test-selenium/bin/chromedriver.exe");
        //WebDriver driver = new ChromeDriver();
        WebDriver driver = new FirefoxDriver();
        WebDriverHelper helper = new WebDriverHelper(driver);
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
        e = helper.waitForElement(By.cssSelector("a[href=\"/j_acegi_logout\"]"), 20);
        e.click();
        // done
        helper.sleepSeconds(10);
        driver.close();
        driver.quit();
        System.out.println("Done");
    }


}
