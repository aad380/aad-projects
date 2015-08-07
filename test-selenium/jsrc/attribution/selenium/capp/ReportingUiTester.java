
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

import attribution.selenium.capp.verifier.*;

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
                        helper_.selectReport("Attribution Summary", true);
                        (new AttributionSummaryReportVerifier(driver_, helper_)).verify(testParameters);;
                    } else if ("PlayerAttribution".equalsIgnoreCase(reportName)) {
                        helper_.selectReport("Player Attribution", true);
                        (new PlayerAttributionReportVerifier(driver_, helper_)).verify(testParameters);;
                    } else if ("PlayerEfficiency".equalsIgnoreCase(reportName)) {
                        helper_.selectReport("Player Efficiency", true);
                        (new PlayerEfficiencyReportVerifier(driver_, helper_)).verify(testParameters);;
                    } else if ("PlayerComparison".equalsIgnoreCase(reportName)) {
                        helper_.selectReport("Player Comparison", true);
                        (new PlayerComparisonReportVerifier(driver_, helper_)).verify(testParameters);;
                    } else if ("PlayerSummary".equalsIgnoreCase(reportName)) {
                        helper_.selectReport("Player Summary", true);
                        (new PlayerSummaryReportVerifier(driver_, helper_)).verify(testParameters);;
                    } else if ("EventComparison".equalsIgnoreCase(reportName)) {
                        helper_.selectReport("Event Comparison", true);
                        (new EventComparisonReportVerifier(driver_, helper_)).verify(testParameters);;
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

