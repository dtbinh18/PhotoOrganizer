/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hoamua.photoorganizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration Reader
 * 
 * @author daothanhbinh
 */
public class PhotoConfig {
    private static final Logger logger = Logger.getLogger(PhotoConfig.class.getName());

    /**
     * 
     */
    public static Properties prop;
    
    /**
     * 
     */
    static {
        try {
            File configFile = new File("./photo-organizer.conf");
            if (! configFile.exists()) {
                throw new IOException("Configuration file does not exists, " + configFile.getAbsolutePath());
            }
            //load a properties file
            prop = new Properties();
            prop.load(new FileInputStream(configFile.getAbsolutePath()));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Cannot read configuration file", ex);
            System.exit(1);
        }
    }
    /**
     * 
     */
    PhotoConfig() {
    }

    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public static String getString(String key, String defaultValue) {
        String result = prop.getProperty(key);
        logger.log(Level.INFO, key + ": " + result);
        if (result == null) {
            return defaultValue;
        } else {
            return result;
        }
    }
    
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public static int getInt(String key, int defaultValue) {
        try{
            return Integer.valueOf(getString(key, "x"));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public static boolean getBool(String key, boolean defaultValue) {
            return Boolean.valueOf(getString(key, "" + defaultValue));
    }
}
