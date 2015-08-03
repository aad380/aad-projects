
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

public class ReportTestExample {

    private static final Logger LOGGER = Logger.getLogger(ReportTestExample.class);
    //public static String CAPP_LOGIN_URL = "https://localhost:8043/login";
    public static String CAPP_LOGIN_FORM_URL = "https://ec2-54-67-77-250.us-west-1.compute.amazonaws.com/login";
    public static String CAPP_USER = "alexander.dudarenko@abakus.me";
    public static String CAPP_PASSWORD = "welcome";

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

    public static class ReportCheckData {

        private Map<String, String> data_ = new HashMap<> ();
        private String fileName_;

        public ReportCheckData (String dataFileName) {
            fileName_ = dataFileName;
            File dataFile = new File (dataFileName);
            try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("#") || line.isEmpty()) {
                        continue;
                    }
                    String[] cols = line.split("\\s*[:=\\|]\\s*", 0);
                    if (cols.length != 2) {
                        throw new RuntimeException("Incorrect report data file:  file=" + fileName_ + ", line=" + line);
                    }
                    data_.put(cols[0].trim(), cols[1].trim());
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public boolean a(String key) {
            return data_.containsKey(key);
        }

        public String getStringValue(String key) {
           String value = data_.get(key);
           if (value == null) {
                throw new RuntimeException("Can't find data: file=" + fileName_ + ", key=" + key);
           }
           return value;
        }

        public int getIntegerValue(String key) {
           String stringValue = this.getStringValue(key);
           try {
               return Integer.parseInt(stringValue);
           } catch (NumberFormatException ex) {
                throw new RuntimeException("Incorrect int value: file=" + fileName_ + ", key=" + key);
               
           }
        }

        public long getLongValue(String key) {
           String stringValue = this.getStringValue(key);
           try {
               return Long.parseLong(stringValue);
           } catch (NumberFormatException ex) {
                throw new RuntimeException("Incorrect long value: file=" + fileName_ + ", key=" + key);
               
           }
        }

        public double getDoubleValue(String key) {
           String stringValue = this.getStringValue(key);
           try {
               return Double.parseDouble(stringValue);
           } catch (NumberFormatException ex) {
                throw new RuntimeException("Incorrect double value: file=" + fileName_ + ", key=" + key);
               
           }
        }

    }

    public static class TestSteps implements Iterable<TestSteps.Step> {
        
        public static class Step {

            private String name_;
            private String[] args_;
            
            public Step (String name, String[] args) {
                name_ = name;
                args_ = args;
            }

            public String getName() {
                return name_;
            }

            public String[] getArgs() {
                return args_;
            }
            
        }

        private List<Step> steps_;

        public TestSteps () {
            steps_ = new ArrayList<>();
        }
        
        public void addStep(Step step) {
            steps_.add(step);
        }

        public Step getStep(int i) {
            return steps_.get(i);
        }

        public int size() {
            return steps_.size();
        }
        
        @Override
        public Iterator<Step> iterator() {
            return steps_.iterator();
        }

    }

    public ReportTestExample (String configFileName) {
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
    }

    public ReportTestExample (String loginFormUrl, String user, String password) {
        loginFormUrl_ = loginFormUrl;
        user_ = user;
        password_ = password;
        //System.setProperty("webdriver.chrome.driver", "C:/home/aad/workspace/aad-projects/test-selenium/bin/chromedriver.exe");
        //driver_ = new ChromeDriver();
        driver_ = new FirefoxDriver();
        helper_ = new WebDriverHelper(driver_);
    }

    public void run () {
        for (TestSteps.Step step : steps_) {
            String name = step.getName();
            String[] args = step.getArgs();
            switch (step.getName()) {
            case "Login":
                if (args.length != 3) {
                    throw new RuntimeException ("Login - Arrgument count must be 3");
                }
                helper_.login(args[0], args[1], args[2]);
                loginFormUrl_ = args[0];
                user_ = args[1];
                password_ = args[2];
                isLogged_ = true;
                helper_.waitForReportLoadingStart(30, false);
                helper_.waitForReportLoadingDone(120, true);
                break;
            case "Logout":
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
                helper_.selectJob(args[0], args[1], args[2]);
                currentClient_ = args[0];
                currentCampaign_ = args[1];
                currentSubCampaign_ = args[2];
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
                ReportCheckData reportCheckData =  new ReportCheckData(dataFileName);
                if ("AttributionSummary".equalsIgnoreCase(reportName)) {
                    checkAttributionSummary(reportCheckData);
                } else if ("PlayerAttributiony".equalsIgnoreCase(reportName)) {
                    checkPlayerAttribution(reportCheckData);
                } else if ("PlayerEfficiency".equalsIgnoreCase(reportName)) {
                    checkPlayerEfficiency(reportCheckData);
                } else {
                    throw new RuntimeException ("VerifyReport - incorrect report name <" + reportName + ">");
                }
                break;
            } default:
                throw new RuntimeException ("Unknown command - " + name);
            }
            
        }
    }

    public void checkAttributionSummary (ReportCheckData rcd) {
        // prepare example values
        int correct_allConverters = rcd.getIntegerValue("allConverters");
        int correct_attributedConverters = rcd.getIntegerValue("attributedConvertersExample");
        int correct_baselineConverters = rcd.getIntegerValue("baselineConvertersExample");
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
    }

    public void checkPlayerAttribution (ReportCheckData rcd) {
        /* pathes for tables
            xpath: //div[@id='table_wrapper']/div[4]/div/div/table/thead/tr[3]/td[2]
            css: "div#table_wrapper div.dataTables_scrollHeadInner table.dataTable"
            xpath://table[@id='table']/tbody/tr[2]/td[2]
            css: div#table_wrapper div.dataTables_scrollBody table.dataTable
        */
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
    }

    public void checkPlayerEfficiency (ReportCheckData rcd) {
        helper_.selectReport("Player Efficiency", true);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: ReportTester PARAMETERS-FILE");
            System.exit(1);
        }
        ReportTestExample rt = new ReportTestExample (args[0]);
        rt.run();
        //LOGGER.info("START TESTS");
        //rt.run("Avis", "NL", "Aug25-Sep21_NL");
        //rt.run("Avis", "UK", "May05-May18_UK");
        //rt.run("Avis", "UK", "Sep15-Sep28_UK");
        //rt.run("Quantcast", "Careers", "Jul 01-Jul 31, 2014");
    }

}

