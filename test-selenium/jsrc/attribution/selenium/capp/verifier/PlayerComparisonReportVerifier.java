package attribution.selenium.capp.verifier;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import attribution.selenium.capp.TestParameters;
import attribution.selenium.utils.WebDriverHelper;

public class PlayerComparisonReportVerifier extends ReportVerifier {

    private static final Logger LOGGER = Logger.getLogger(PlayerComparisonReportVerifier.class);

    public PlayerComparisonReportVerifier(WebDriver driver, WebDriverHelper helper) {
        super(driver, helper);
    }

    @Override
    public void verify(TestParameters rcd) {
        LOGGER.info("REPORT-TEST: Player Comparison");
        // prepare example values
        String[] testdata_allPlayers = parseArrayParameter(rcd.getStringValue("allPlayers"));
        if (testdata_allPlayers.length != 3) {
            throw new RuntimeException("Incorrect test data: allPlayers=" + rcd.getStringValue("allPlayers"));
        }
        // table elements
        WebElement top_table = helper_.waitForElement(By.cssSelector("div#table_wrapper div.dataTables_scrollHeadInner table.dataTable"), 20);
        if (top_table == null) {
            throw new NoSuchElementException("checkPlayerComparisonReport: cant't find top_table.");
        }
        WebElement bottom_table = helper_.waitForElement(By.cssSelector("div#table_wrapper div.dataTables_scrollBody table.dataTable"), 20);
        if (bottom_table == null) {
            throw new NoSuchElementException("checkPlayerComparisonReport: cant't find bottom_table.");
        }
        WebElement e;
        //
        // All Players
        //
        // allPlayers - CPA
        e = top_table.findElement(By.xpath(".//thead/tr[2]/td[2]"));
        if (e == null) {
            throw new NoSuchElementException("checkPlayerComparisonReport: cant't find allPlayers-CPA element.");
        }
        if (!helper_.getWebElementText(e).trim().equals(testdata_allPlayers[0])) {
            throw new RuntimeException("checkPlayersComparisonReport: incorrect allPlayers-CPA value.");
        }
        // allPlayers - Exposed Converters
        e = top_table.findElement(By.xpath(".//thead/tr[2]/td[3]"));
        if (e == null) {
            throw new NoSuchElementException("checkPlayerComparisonReport: cant't find allPlayers-ExposedConverters element.");
        }
        if (!helper_.getWebElementText(e).trim().equals(testdata_allPlayers[1])) {
            throw new RuntimeException("checkPlayersComparisonReport: incorrect allPlayers-ExposedConverters value.");
        }
        // allPlayers - Attributed Converters
        e = top_table.findElement(By.xpath(".//thead/tr[2]/td[4]"));
        if (e == null) {
            throw new NoSuchElementException("checkPlayerComparisonReport: cant't find allPlayers-AttributedConverters element.");
        }
        if (!helper_.getWebElementText(e).trim().equals(testdata_allPlayers[2])) {
            throw new RuntimeException("checkPlayersComparisonReport: incorrect allPlayers-AttributedConverters value.");
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
                throw new RuntimeException("checkPlayersComparisonReport: can't find table row for player <" + playerName +">");
            }
            // player - CPA 
            e = rowElement.findElement(By.xpath(".//td[2]"));
            if (e == null) {
                throw new NoSuchElementException("checkPlayerComparisonReport: cant't find <"+playerName+">-CPA element.");
            }
            if (!helper_.getWebElementText(e).trim().equals(playerData[0])) {
                throw new RuntimeException("checkPlayersComparisonReport: incorrect <"+playerName+">-CPA value.");
            }
            // player - Exposed Converters
            e = rowElement.findElement(By.xpath(".//td[3]"));
            if (e == null) {
                throw new NoSuchElementException("checkPlayerComparisonReport: cant't find <"+playerName+">-ExposedConverters element.");
            }
            if (!helper_.getWebElementText(e).trim().equals(playerData[1])) {
                throw new RuntimeException("checkPlayersComparisonReport: incorrect <"+playerName+">-ExposedConverters value.");
            }
            // player - Attributed Converters
            e = rowElement.findElement(By.xpath(".//td[4]"));
            if (e == null) {
                throw new NoSuchElementException("checkPlayerComparisonReport: cant't find <"+playerName+">-AttributedConverters element.");
            }
            if (!helper_.getWebElementText(e).trim().equals(playerData[2])) {
                throw new RuntimeException("checkPlayersComparisonReport: incorrect <"+playerName+">-AttributedConverters value.");
            }
        }
        // done
        LOGGER.info("REPORT-OK: Player Comparison");
    }

}
