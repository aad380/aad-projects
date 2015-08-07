package attribution.selenium.capp.verifier;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import attribution.selenium.capp.TestParameters;
import attribution.selenium.utils.WebDriverHelper;

/**
 * Copyright (c) 2013-2015 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 8/7/2015
 */
public class AttributionSummaryReportVerifier extends ReportVerifier {

    private static final Logger LOGGER = Logger.getLogger(AttributionSummaryReportVerifier.class);

    public AttributionSummaryReportVerifier (WebDriver driver, WebDriverHelper helper) {
        super (driver, helper);
    }

    @Override
    public  void verify(TestParameters rcd) {
        LOGGER.info("REPORT-TEST: Attribution Summary");
        // prepare example values
        int correct_allConverters = rcd.getIntegerValue("allConverters");
        int correct_attributedConverters = rcd.getIntegerValue("attributedConverters");
        int correct_baselineConverters = rcd.getIntegerValue("baselineConverters");
        // check report
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
        if (allConverters != correct_allConverters) {
            throw new RuntimeException("checkAttributionSummaryReport: incorrect all converters value.");
        }
        // check attributed converters
        e = table.findElement(By.xpath(".//tbody/tr/td[1]"));
        if (e == null) {
            throw new NoSuchElementException("checkAttributionSummaryReport: cant't find attributedConverters element.");
        }
        int attributedConverters = helper_.getWebElementInteger(e);
        if (attributedConverters != correct_attributedConverters) {
            throw new RuntimeException("checkAttributionSummaryReport: incorrect attributed converters value.");
        }
        // check baseline converters
        e = table.findElement(By.xpath(".//tbody/tr/td[2]"));
        if (e == null) {
            throw new NoSuchElementException("checkAttributionSummaryReport: cant't find baselineConverters element.");
        }
        int baselineConverters = helper_.getWebElementInteger(e);
        if (baselineConverters != correct_baselineConverters) {
            throw new RuntimeException("checkAttributionSummaryReport: incorrect baseline converters value.");
        }
        if (allConverters != attributedConverters + baselineConverters) {
            throw new RuntimeException("checkAttributionSummaryReport: allConverters("+allConverters+") != attributedConverters("+attributedConverters+") + baselineConverters("+baselineConverters+")");
        }
        LOGGER.info("REPORT-OK: Attribution Summary");
    }

}
