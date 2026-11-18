package com.automation.hooks;

import com.automation.config.ConfigReader;
import com.automation.utils.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class Hooks {

    private static final AtomicBoolean setupDone = new AtomicBoolean(false);

    @Before(order = 0)
    public void setupAllureMetadata() {
        if (setupDone.compareAndSet(false, true)) {
            restoreAllureHistory();
            writeEnvironment(null, null);   // static props only; UI hook overwrites with browser info
        }
    }

    @Before(value = "@ui", order = 1)
    public void initBrowser(Scenario scenario) {
        if (DriverManager.isInitialized()) return;   // prevent double browser launch

        String browser = System.getProperty("browser", ConfigReader.getBrowser());
        DriverManager.initDriver(browser);

        String browserName    = DriverManager.getBrowserName();
        String browserVersion = DriverManager.getBrowserVersion();
        String displayName    = Character.toUpperCase(browserName.charAt(0)) + browserName.substring(1);

        // Unique historyId per browser+scenario so Chrome and Firefox runs of the
        // same scenario appear as SEPARATE entries in the Allure report (not retries).
        Allure.label("historyId",   String.valueOf(Math.abs((browserName + ":" + scenario.getId()).hashCode())));
        Allure.label("parentSuite", displayName);   // groups tests under Chrome / Firefox in Suites view
        Allure.label("browser",     browserName);
        Allure.label("tag",         displayName);   // coloured badge in test list
        Allure.parameter("Browser",         displayName);
        Allure.parameter("Browser Version", browserVersion);

        // Overwrite environment.properties with ALL keys (static + browser).
        // Single atomic write avoids the load-modify-save race condition that
        // was causing only Browser.Name/Version to appear in the ENVIRONMENT widget.
        writeEnvironment(browserName, browserVersion);
    }

    @After(value = "@ui", order = 1)
    public void tearDownBrowser(Scenario scenario) {
        if (scenario.isFailed() && DriverManager.isInitialized()) {
            byte[] screenshot = ((TakesScreenshot) DriverManager.getDriver())
                    .getScreenshotAs(OutputType.BYTES);
            Allure.getLifecycle().addAttachment(
                    "Screenshot — " + scenario.getName(), "image/png", "png", screenshot);
        }
        DriverManager.quitDriver();
    }

    private void restoreAllureHistory() {
        Path src      = Paths.get("allure-history");
        String resDir = System.getProperty("allure.results.directory", "target/allure-results");
        Path dest     = Paths.get(resDir, "history");
        if (!Files.exists(src)) return;
        try {
            Files.createDirectories(dest);
            try (Stream<Path> walk = Files.walk(src)) {
                walk.forEach(s -> {
                    Path d = dest.resolve(src.relativize(s));
                    try {
                        if (Files.isDirectory(s)) Files.createDirectories(d);
                        else Files.copy(s, d, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ignored) {}
                });
            }
        } catch (IOException ignored) {}
    }

    /**
     * Writes environment.properties in a single pass.
     * Pass null for browserName/browserVersion to omit browser keys (API-only runs).
     * Because this is a full overwrite (not load-modify-save), there is no race
     * condition between parallel JVM forks — the last writer wins, which is always
     * one of the UI forks (they start browsers and therefore write last).
     */
    private void writeEnvironment(String browserName, String browserVersion) {
        String dir  = System.getProperty("allure.results.directory", "target/allure-results");
        Path   file = Paths.get(dir, "environment.properties");
        try {
            Files.createDirectories(file.getParent());
            Properties env = new Properties();
            env.setProperty("Application.URL", ConfigReader.getBaseUrl());
            env.setProperty("API.Base.URL",    ConfigReader.getApiBaseUrl());
            env.setProperty("OS",              System.getProperty("os.name") + " " + System.getProperty("os.version"));
            env.setProperty("Java.Version",    System.getProperty("java.version"));
            if (browserName != null) {
                env.setProperty("Browser.Name",    browserName);
                env.setProperty("Browser.Version", browserVersion);
            }
            try (OutputStream out = Files.newOutputStream(file)) {
                env.store(out, null);
            }
        } catch (IOException ignored) {}
    }
}
