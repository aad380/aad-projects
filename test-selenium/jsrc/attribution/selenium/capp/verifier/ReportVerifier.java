package attribution.selenium.capp.verifier;

import org.openqa.selenium.WebDriver;

import attribution.selenium.capp.TestParameters;
import attribution.selenium.utils.WebDriverHelper;

/**
 * Copyright (c) 2013-2015 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 8/7/2015
 */
public abstract class ReportVerifier {

    protected WebDriver driver_;
    protected WebDriverHelper helper_;

    public ReportVerifier (WebDriver driver, WebDriverHelper helper) {
        driver_ = driver;
        helper_ = helper;
    }

    public abstract void verify(TestParameters rcd);

    protected static String[] parseArrayParameter(String text) {
        String[] array = text.split("\\|");
        for (int i = 0; i < array.length; ++ i) {
            array[i] = array[i].trim();
        }
        return array;
    }

}
