package attribution.selenium.capp.verifier;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


import attribution.selenium.capp.TestParameters;
import attribution.selenium.utils.WebDriverHelper;
import static attribution.selenium.utils.WebDriverHelper.trim;

/**
 * Copyright (c) 2013-2015 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 8/7/2015
 */
public class EventComparisonReportVerifier extends ReportVerifier {

    private static final Logger LOGGER = Logger.getLogger(AttributionSummaryReportVerifier.class);

    public EventComparisonReportVerifier(WebDriver driver, WebDriverHelper helper) {
        super(driver, helper);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void verify(TestParameters rcd) {
        LOGGER.info("REPORT-TEST: Event Comparison");
        // table elements
        WebElement table = helper_.waitForElement(By.cssSelector("div#table_wrapper div.dataTables_scrollBody table.dataTable"), 20);
        if (table == null) {
            throw new NoSuchElementException("checkEventComparisonReport: cant't find top_table.");
        }
        //
        // other players
        //
        WebElement e;
        Map<String,String> bundle = rcd.getBoundle("player");
        for (String playerName : bundle.keySet()) {
            String[] playerData = parseArrayParameter(bundle.get(playerName));
            WebElement rowElement = null;
            for (WebElement tr : table.findElements(By.xpath(".//tbody/tr"))) {
                WebElement nameElement = tr.findElement(By.xpath(".//td[1]"));
                if (helper_.getWebElementText(nameElement).trim().equals(playerName)) {
                    rowElement = tr;
                    break;
                }
            }
            if (rowElement == null) {
                throw new RuntimeException("checkEventComparisonReport: can't find table row for player <" + playerName +">");
            }
            for (int i = 0; i < playerData.length; ++ i) {
                int columnNumber = 2 + i;
                e = rowElement.findElement(By.xpath(".//td[" + columnNumber + "]"));
                if (e == null) {
                    throw new NoSuchElementException("checkEventComparisonReport: cant't find \""+playerName+"\"["+i+"] element.");
                }
                String cellValue = trim(helper_.getWebElementText(e));
                String neededValue = playerData[i];
                if (!neededValue.equals(cellValue)) {
                    throw new RuntimeException("checkEventComparisonReport: incorrect \""+playerName+"\"["+i+"] value. Cell value <"+cellValue+"> != needed value <"+neededValue+">");
                }
            }
        }
        // done
        LOGGER.info("REPORT-OK: Event Comparison");
    }

}
