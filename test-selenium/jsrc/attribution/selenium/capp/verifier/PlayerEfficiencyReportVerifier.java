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
public class PlayerEfficiencyReportVerifier extends ReportVerifier {

    private static final Logger LOGGER = Logger.getLogger(PlayerAttributionReportVerifier.class);

    public PlayerEfficiencyReportVerifier(WebDriver driver, WebDriverHelper helper) {
        super(driver, helper);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void verify(TestParameters rcd) {
        LOGGER.info("REPORT-TEST: Player Efficiency");
        // table elements
        WebElement table = helper_.waitForElement(By.cssSelector("div#table_wrapper div.dataTables_scrollBody table#table.dataTable"), 20);
        if (table == null) {
            throw new NoSuchElementException("checkPlayerEfficiencyReport: cant't find table.");
        }
        //
        // check players
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
                throw new RuntimeException("checkPlayerEfficiencyReport: can't find table row for player <" + playerName +">");
            }
            // player - CPA 
            e = rowElement.findElement(By.xpath(".//td[2]"));
            if (e == null) {
                throw new NoSuchElementException("checkPlayerEfficiencyReport: cant't find <"+playerName+">-CPA element.");
            }
            if (!helper_.getWebElementText(e).trim().equals(playerData[0])) {
                throw new RuntimeException("checkPlayerEfficiencyReport: incorrect <"+playerName+">-CPA value.");
            }
            // player - Marketing Spend
            e = rowElement.findElement(By.xpath(".//td[3]"));
            if (e == null) {
                throw new NoSuchElementException("checkPlayerEfficiencyReport: cant't find <"+playerName+">-MarketingSpend element.");
            }
            if (!helper_.getWebElementText(e).trim().equals(playerData[1])) {
                throw new RuntimeException("checkPlayerEfficiencyReport: incorrect <"+playerName+">-MarketingSpend value.");
            }
            // player - Attributed Converters
            e = rowElement.findElement(By.xpath(".//td[4]"));
            if (e == null) {
                throw new NoSuchElementException("checkPlayerEfficiencyReport: cant't find <"+playerName+">-AttributedConverters element.");
            }
            if (!helper_.getWebElementText(e).trim().equals(playerData[2])) {
                throw new RuntimeException("checkPlayerEfficiencyReport: incorrect <"+playerName+">-AttributedConverters value.");
            }
        }
        // done
        LOGGER.info("REPORT-OK: Player Efficiency");
    }

}
