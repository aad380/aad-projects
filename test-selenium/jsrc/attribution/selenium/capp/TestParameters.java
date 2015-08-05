package attribution.selenium.capp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2013-2015 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 7/23/2015
 */
class TestParameters {

    private Map<String, String> data_ = new HashMap<> ();
    private String fileName_;

    public TestParameters (String dataFileName) {
        fileName_ = dataFileName;
        File dataFile = new File (dataFileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }
                String[] cols = line.split("\\s*[:=]\\s*", 0);
                if (cols.length != 2) {
                    throw new RuntimeException("Incorrect report data file:  file=" + fileName_ + ", line=" + line);
                }
                data_.put(cols[0].trim(), cols[1].trim());
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean hasKey(String key) {
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

    public Map<String,String> getBoundle(String bundleName) {
        if (!bundleName.endsWith(".")) {
            bundleName = bundleName + ".";
        }
        Map<String,String> map = new HashMap<>();
        for (Map.Entry<String, String> e : data_.entrySet()) {
            if (e.getKey().startsWith(bundleName)) {
                String key = e.getKey().substring(bundleName.length());
                map.put(key, e.getValue());
            }
        }
        return map;
    }

}
