
package attribution.selenium.capp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import attribution.selenium.utils.WebDriverHelper;
import static attribution.selenium.utils.WebDriverHelper.trim;

/**
 * Copyright (c) 2013-2015 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 7/23/2015
 */
public class ReportingUiTester {

    private static final Logger LOGGER = Logger.getLogger(ReportingUiTester.class);

    private boolean useChrome_ = false;
    private String chromeDriverPath_ = null;

    private WebDriver driver_;
    private WebDriverHelper helper_;
    
    // current state
    private String loginFormUrl_;
    private String user_;
    private String password_;
    private String currentClient_;
    private String currentCampaign_;
    private String currentSubCampaign_;

    private File configFile_;
    private boolean isLogged_ = false;
    TestSteps steps_;
    
    public ReportingUiTester (String configFileName) {
        try {
            steps_ = new TestSteps();
            configFile_ = new File(configFileName);
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile_))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("#") || line.isEmpty()) {
                        continue;
                    }
                    String[] cols = line.split("\\|", 0);
                    if (cols.length < 1) {
                        throw new RuntimeException("Incorrect line: " + line);
                    }
                    if ("Driver".equalsIgnoreCase(cols[0])) {
                        if (cols.length < 2) {
                            throw new RuntimeException("Driver - driver type (Firefox or Chrome) must be specified");
                        }
                        if ("Chrome".equalsIgnoreCase(cols[1])) {
                            if (cols.length < 3) {
                                throw new RuntimeException("Driver - driver executable must be specified for Chrome Driver");
                            }
                            useChrome_ = true;
                            chromeDriverPath_ = cols[2];
                        } else if ("Firefox".equalsIgnoreCase(cols[1])) {
                            useChrome_ = false;
                        } else {
                            throw new RuntimeException("Driver - incorrect driver name <" + cols[1] + ">, should be Firefox or Chrome.");
                        }
                    } else {
                        steps_.addStep(new TestSteps.Step(cols[0], Arrays.copyOfRange(cols, 1, cols.length)));
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            // init driver & helper
            if (useChrome_) {
                System.setProperty("webdriver.chrome.driver", chromeDriverPath_);
                driver_ = new ChromeDriver();
            } else {
                driver_ = new FirefoxDriver();
            }
            driver_.manage().window().setPosition(new Point(0,0));
            driver_.manage().window().setSize(new Dimension(1550, 850));
            helper_ = new WebDriverHelper(driver_);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
            throw new RuntimeException (ex);
        }
    }

    public void run () {
        try {
            LOGGER.info("START TESTS");
            for (TestSteps.Step step : steps_) {
                String name = step.getName();
                String[] args = step.getArgs();
                switch (step.getName()) {
                case "Login":
                    if (args.length != 3) {
                        throw new RuntimeException ("Login - Arrgument count must be 3");
                    }
                    loginFormUrl_ = args[0];
                    user_ = args[1];
                    password_ = args[2];
                    LOGGER.info("LOGIN: user=" + user_ + " url=" + loginFormUrl_);
                    helper_.login(loginFormUrl_, user_, password_);
                    isLogged_ = true;
                    helper_.waitForReportLoadingStart(30, false);
                    helper_.waitForReportLoadingDone(120, true);
                    break;
                case "Logout":
                    LOGGER.info("LOGOUT");
                    helper_.logout();
                    loginFormUrl_ = null;
                    user_ = null;
                    password_ = null;
                    isLogged_ = false;
                    break;
                case "SelectJob":
                    if (args.length != 3) {
                        throw new RuntimeException ("SelectJob - Arrgument count must be 3");
                    }
                    currentClient_ = args[0];
                    currentCampaign_ = args[1];
                    currentSubCampaign_ = args[2];
                    LOGGER.info("SELECT-JOB: " + currentClient_ + "/" + currentCampaign_ + "/" + currentSubCampaign_);
                    helper_.selectJob(currentClient_, currentCampaign_, currentSubCampaign_);
                    break;
                case "VerifyReport": {
                    if (args.length != 2) {
                        throw new RuntimeException ("SelectReport - Arrgument count must be 2");
                    }
                    String reportName = args[0].replaceAll("\\s+", "");
                    String dataFileName = args[1];
                    if (!dataFileName.matches("^(?i)(/|\\\\|([A-Z]:))")) {
                        //dataFileName = (baseDir + File.separator + dataFileName);
                        Path baseDirPath = Paths.get(configFile_.getParentFile().getAbsolutePath());
                        dataFileName = baseDirPath.resolve(dataFileName).normalize().toString();;
                    }
                    TestParameters testParameters =  new TestParameters(dataFileName);
                    if ("AttributionSummary".equalsIgnoreCase(reportName)) {
                        checkAttributionSummary(testParameters);
                    } else if ("PlayerAttribution".equalsIgnoreCase(reportName)) {
                        checkPlayerAttribution(testParameters);
                    } else if ("PlayerEfficiency".equalsIgnoreCase(reportName)) {
                        checkPlayerEfficiency(testParameters);
                    } else if ("PlayerComparison".equalsIgnoreCase(reportName)) {
                        checkPlayerComparison(testParameters);
                    } else if ("PlayerSummary".equalsIgnoreCase(reportName)) {
                        checkPlayerSummary(testParameters);
                    } else if ("EventComparison".equalsIgnoreCase(reportName)) {
                        checkEventComparison(testParameters);
                    } else {
                        throw new RuntimeException ("VerifyReport - incorrect report name <" + reportName + ">");
                    }
                    break;
                } default:
                    throw new RuntimeException ("Unknown command - " + name);
                }
            }
            LOGGER.info("END.");
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
            throw new RuntimeException (ex);
        }
    }

    private void checkAttributionSummary (TestParameters rcd) {
        LOGGER.info("REPORT-TEST: Attribution Summary");
        // prepare example values
        int correct_allConverters = rcd.getIntegerValue("allConverters");
        int correct_attributedConverters = rcd.getIntegerValue("attributedConverters");
        int correct_baselineConverters = rcd.getIntegerValue("baselineConverters");
        // check report
        helper_.selectReport("Attribution Summary", true);
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

    private void checkPlayerAttribution (TestParameters rcd) {
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
        helper_.selectReport("Player Attribution", true);
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

    private void checkPlayerEfficiency (TestParameters rcd) {
        LOGGER.info("REPORT-TEST: Player Efficiency");
        helper_.selectReport("Player Efficiency", true);
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

    private void checkPlayerComparison (TestParameters rcd) {
        LOGGER.info("REPORT-TEST: Player Comparison");
        // prepare example values
        String[] testdata_allPlayers = parseArrayParameter(rcd.getStringValue("allPlayers"));
        if (testdata_allPlayers.length != 3) {
            throw new RuntimeException("Incorrect test data: allPlayers=" + rcd.getStringValue("allPlayers"));
        }
        // table elements
        helper_.selectReport("Player Comparison", true);
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

    private void checkPlayerSummary (TestParameters rcd) {
        LOGGER.info("REPORT-TEST: Player Summary");
        // prepare example values
        String[] testdata_allPlayers = parseArrayParameter(rcd.getStringValue("allPlayers"));
        // table elements
        helper_.selectReport("Player Summary", true);
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

    private void checkEventComparison (TestParameters rcd) {
        LOGGER.info("REPORT-TEST: Event Comparison");
        // table elements
        helper_.selectReport("Event Comparison", true);
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
                throw new RuntimeException("checkPlayersComparisonReport: can't find table row for player <" + playerName +">");
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

//System.err.println("<"+helper_.getWebElementText(e).trim() + "><" + playerData[0] +">");

    private static String[] parseArrayParameter(String text) {
        String[] array = text.split("\\|");
        for (int i = 0; i < array.length; ++ i) {
            array[i] = array[i].trim();
        }
        return array;
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        if (args.length != 1) {
            System.err.println("Usage: ReportTester PARAMETERS-FILE");
            System.exit(1);
        }
        ReportingUiTester rt = new ReportingUiTester (args[0]);
        rt.run();
        //"Avis", "NL", "Aug25-Sep21_NL"
        //"Avis", "UK", "May05-May18_UK"
        //"Avis", "UK", "Sep15-Sep28_UK"
        //"Quantcast", "Careers", "Jul 01-Jul 31, 2014"
    }

}

