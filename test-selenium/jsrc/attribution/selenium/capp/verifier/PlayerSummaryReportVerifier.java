package attribution.selenium.capp.verifier;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import attribution.selenium.capp.TestParameters;
import attribution.selenium.utils.WebDriverHelper;

import static attribution.selenium.utils.WebDriverHelper.trim;

import java.util.Map;

/**
 * Copyright (c) 2013-2015 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 8/7/2015
 */
public class PlayerSummaryReportVerifier extends ReportVerifier {

    private static final Logger LOGGER = Logger.getLogger(PlayerAttributionReportVerifier.class);

    public PlayerSummaryReportVerifier(WebDriver driver, WebDriverHelper helper) {
        super(driver, helper);
    }

    @Override
    public void verify(TestParameters rcd) {
        LOGGER.info("REPORT-TEST: Player Summary");
        // prepare example values
        String[] testdata_allPlayers = parseArrayParameter(rcd.getStringValue("allPlayers"));
        // table elements
        WebElement top_table = helper_.waitForElement(By.cssSelector("div#table_wrapper div.dataTables_scrollHeadInner table.dataTable"), 20);
        if (top_table == null) {
            throw new NoSuchElementException("checkPlayerSummaryReport: cant't find top_table.");
        }
        WebElement bottom_table = helper_.waitForElement(By.cssSelector("div#table_wrapper div.dataTables_scrollBody table.dataTable"), 20);
        if (bottom_table == null) {
            throw new NoSuchElementException("checkPlayerSummaryReport: cant't find bottom_table.");
        }
        WebElement e;
        //
        // All Players
        //
        for (int i = 0; i < testdata_allPlayers.length; ++ i) {
            int columnNumber = 2 + i;
            e = top_table.findElement(By.xpath(".//thead/tr[3]/td[" + columnNumber + "]"));
            if (e == null) {
                throw new NoSuchElementException("checkPlayerSummaryReport: cant't find allPlayers["+i+"] element.");
            }
            String cellValue = trim(helper_.getWebElementText(e));
            String neededValue = testdata_allPlayers[i];
            if (!neededValue.equals(cellValue)) {
                throw new RuntimeException("checkPlayersSummaryReport: incorrect allPlayers["+i+"] value. Cell value <"+cellValue+"> != needed value <"+neededValue+">");
            }
        }
        //
        // other players
        //
        Map<String,String> bundle = rcd.getBoundle("player");
        for (String playerName : bundle.keySet()) {
            String[] playerData = parseArrayParameter(bundle.get(playerName));
            WebElement rowElement = null;
            for (WebElement tr : bottom_table.findElements(By.xpath(".//tbody/tr"))) {
                WebElement nameElement = tr.findElement(By.xpath(".//td[1]"));
                if (helper_.getWebElementText(nameElement).trim().equals(playerName)) {
                    rowElement = tr;
                    break;
                }
            }
            if (rowElement == null) {
                throw new RuntimeException("checkPlayersSummaryReport: can't find table row for player <" + playerName +">");
            }
            for (int i = 0; i < playerData.length; ++ i) {
                int columnNumber = 2 + i;
                e = rowElement.findElement(By.xpath(".//td[" + columnNumber + "]"));
                if (e == null) {
                    throw new NoSuchElementException("checkPlayerSummaryReport: cant't find \""+playerName+"\"["+i+"] element.");
                }
                String cellValue = trim(helper_.getWebElementText(e));
                String neededValue = playerData[i];
                if (!neededValue.equals(cellValue)) {
                    throw new RuntimeException("checkPlayersSummaryReport: incorrect \""+playerName+"\"["+i+"] value. Cell value <"+cellValue+"> != needed value <"+neededValue+">");
                }
            }
        }
        // done
        LOGGER.info("REPORT-OK: Player Summary");
    }

}
