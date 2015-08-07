package attribution.selenium.capp.verifier;

import java.util.Map;

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
public class PlayerAttributionReportVerifier extends ReportVerifier {


    private static final Logger LOGGER = Logger.getLogger(AttributionSummaryReportVerifier.class);

    public PlayerAttributionReportVerifier(WebDriver driver, WebDriverHelper helper) {
        super (driver, helper);
    }

    @Override
    public  void verify(TestParameters rcd) {
        /* pathes for tables
        xpath: //div[@id='table_wrapper']/div[4]/div/div/table/thead/tr[3]/td[2]
        css: "div#table_wrapper div.dataTables_scrollHeadInner table.dataTable"
        xpath://table[@id='table']/tbody/tr[2]/td[2]
        css: div#table_wrapper div.dataTables_scrollBody table.dataTable
        */
        LOGGER.info("REPORT-TEST: Player Attribution");
        // prepare example values
        String[] testdata_allPlayers = parseArrayParameter(rcd.getStringValue("allPlayers"));
        if (testdata_allPlayers.length != 4) {
            throw new RuntimeException("Incorrect test data: allPlayers=" + rcd.getStringValue("allPlayers"));
        }
        // table elements
        WebElement top_table = helper_.waitForElement(By.cssSelector("div#table_wrapper div.dataTables_scrollHeadInner table.dataTable"), 20);
        if (top_table == null) {
            throw new NoSuchElementException("checkPlayerAttributionReport: cant't find top_table.");
        }
        WebElement bottom_table = helper_.waitForElement(By.cssSelector("div#table_wrapper div.dataTables_scrollBody table.dataTable"), 20);
        if (bottom_table == null) {
            throw new NoSuchElementException("checkPlayerAttributionReport: cant't find bottom_table.");
        }
        WebElement e;
        //
        // All Players
        //
        // allPlayers - Total 
        e = top_table.findElement(By.xpath(".//thead/tr[3]/td[2]"));
        if (e == null) {
            throw new NoSuchElementException("checkPlayerAttributionReport: cant't find allPlayers-Total element.");
        }
        if (!helper_.getWebElementText(e).trim().equals(testdata_allPlayers[0])) {
            throw new RuntimeException("checkPlayersAttributionReport: incorrect allPlayers-Total value.");
        }
        // allPlayers - Upper Funnel 
        e = top_table.findElement(By.xpath(".//thead/tr[3]/td[3]"));
        if (e == null) {
            throw new NoSuchElementException("checkPlayerAttributionReport: cant't find allPlayers-UpperFunnel element.");
        }
        if (!helper_.getWebElementText(e).trim().equals(testdata_allPlayers[1])) {
            throw new RuntimeException("checkPlayersAttributionReport: incorrect allPlayers-UpperFunnel value.");
        }
        // allPlayers - Lower Funnel 
        e = top_table.findElement(By.xpath(".//thead/tr[3]/td[4]"));
        if (e == null) {
            throw new NoSuchElementException("checkPlayerAttributionReport: cant't find allPlayers-LowerFunnel element.");
        }
        if (!helper_.getWebElementText(e).trim().equals(testdata_allPlayers[2])) {
            throw new RuntimeException("checkPlayersAttributionReport: incorrect allPlayers-LowerFunnel value.");
        }
        // allPlayers - Percentage of Total
        e = top_table.findElement(By.xpath(".//thead/tr[3]/td[5]"));
        if (e == null) {
            throw new NoSuchElementException("checkPlayerAttributionReport: cant't find allPlayers-PercentageOfTotal element.");
        }
        if (!helper_.getWebElementText(e).trim().equals(testdata_allPlayers[3])) {
            throw new RuntimeException("checkPlayersAttributionReport: incorrect allPlayers-PercentageOfTotal value.");
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
                throw new RuntimeException("checkPlayersAttributionReport: can't find table row for player <" + playerName +">");
            }
            // player - Total 
            e = rowElement.findElement(By.xpath(".//td[2]"));
            if (e == null) {
                throw new NoSuchElementException("checkPlayerAttributionReport: cant't find <"+playerName+">-Total element.");
            }
            if (!helper_.getWebElementText(e).trim().equals(playerData[0])) {
                throw new RuntimeException("checkPlayersAttributionReport: incorrect <"+playerName+">-Total value.");
            }
            // player - Upper Funnel 
            e = rowElement.findElement(By.xpath(".//td[3]"));
            if (e == null) {
                throw new NoSuchElementException("checkPlayerAttributionReport: cant't find <"+playerName+">-UpperFunnel element.");
            }
            if (!helper_.getWebElementText(e).trim().equals(playerData[1])) {
                throw new RuntimeException("checkPlayersAttributionReport: incorrect <"+playerName+">-UpperFunnel value.");
            }
            // player - Lower Funnel 
            e = rowElement.findElement(By.xpath(".//td[4]"));
            if (e == null) {
                throw new NoSuchElementException("checkPlayerAttributionReport: cant't find <"+playerName+">-LowerFunnel element.");
            }
            if (!helper_.getWebElementText(e).trim().equals(playerData[2])) {
                throw new RuntimeException("checkPlayersAttributionReport: incorrect <"+playerName+">-LowerFunnel value.");
            }
            // player - Percentage of Total
            e = rowElement.findElement(By.xpath(".//td[5]"));
            if (e == null) {
                throw new NoSuchElementException("checkPlayerAttributionReport: cant't find <"+playerName+">-PercentageOfTotal element.");
            }
            if (!helper_.getWebElementText(e).trim().equals(playerData[3])) {
                throw new RuntimeException("checkPlayersAttributionReport: incorrect <"+playerName+">-PercentageOfTotal value.");
            }
        }
        // done
        LOGGER.info("REPORT-OK: Player Attribution");
    }

}
