package com.automation.config;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties = new Properties();

    static {
        try (InputStream is = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (is == null) throw new RuntimeException("config.properties not found on classpath");
            properties.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    // System property overrides config file (useful for CI / -D flags)
    public static String get(String key) {
        return System.getProperty(key, properties.getProperty(key));
    }

    public static String getBaseUrl()    { return get("base.url"); }
    public static String getApiBaseUrl() { return get("api.base.url"); }
    public static String getBrowser()    { return get("browser"); }
    public static boolean isHeadless()   { return Boolean.parseBoolean(get("headless")); }
    public static int getImplicitWait()  { return Integer.parseInt(get("implicit.wait")); }
    public static int getExplicitWait()  { return Integer.parseInt(get("explicit.wait")); }
}
