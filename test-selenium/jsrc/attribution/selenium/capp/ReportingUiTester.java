
package attribution.selenium.capp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import attribution.selenium.utils.WebDriverHelper;

/**
 * Copyright (c) 2013-2015 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 7/23/2015
 */
public class ReportingUiTester {

    private static final Logger LOGGER = Logger.getLogger(ReportingUiTester.class);
    private static boolean useChrome_ = false;

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
                    steps_.addStep(new TestSteps.Step(cols[0], Arrays.copyOfRange(cols, 1, cols.length)));
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            // init driver & helper
            if (useChrome_) {
                System.setProperty("webdriver.chrome.driver", "C:/home/aad/workspace/aad-projects/test-selenium/bin/chromedriver.exe");
                driver_ = new ChromeDriver();
            } else {
                driver_ = new FirefoxDriver();
            }
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
        double correct_allPlayersTotal = rcd.getDoubleValue("allPlayersTotal");
        // check report
        helper_.selectReport("Player Attribution", true);
        WebElement top_table = helper_.waitForElement(By.cssSelector("div#table_wrapper div.dataTables_scrollHeadInner table.dataTable"), 20);
        if (top_table == null) {
            throw new NoSuchElementException("checkPlayerAttributionReport: cant't find top_table.");
        }
        WebElement bottom_table = helper_.waitForElement(By.cssSelector("div#table_wrapper div.dataTables_scrollBody table.dataTable"), 20);
        if (bottom_table == null) {
            throw new NoSuchElementException("checkPlayerAttributionReport: cant't find bottom_table.");
        }
        WebElement e = top_table.findElement(By.xpath(".//thead/tr[3]/td[2]"));
        if (e == null) {
            throw new NoSuchElementException("checkPlayerAttributionReport: cant't find allPlayersTotal element.");
        }
        double allPlayersTotal = helper_.getWebElementDouble(e);
        if (allPlayersTotal != 211.0) {
            throw new RuntimeException("checkPlayersAttributionReport: incorrect allPlayersTotal value.");
        }
        LOGGER.info("REPORT-OK: Player Attribution");
    }

    private void checkPlayerEfficiency (TestParameters rcd) {
        LOGGER.info("REPORT-TEST: Player Efficiency");
        helper_.selectReport("Player Efficiency", true);
        LOGGER.info("REPORT-OK: Player Efficiency");
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

