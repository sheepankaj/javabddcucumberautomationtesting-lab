package com.automation.utils;

import com.automation.config.ConfigReader;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.time.Duration;

/**
 * Thread-safe WebDriver factory.
 *
 * Driver management is handled by Selenium Manager (bundled in Selenium 4.6+).
 * No explicit WebDriverManager.setup() calls are needed — Selenium Manager
 * detects the installed browser version and downloads the exactly matching
 * driver binary automatically, eliminating version-mismatch errors.
 */
public class DriverManager {

    private static final ThreadLocal<WebDriver> driverHolder         = new ThreadLocal<>();
    private static final ThreadLocal<String>    browserHolder        = new ThreadLocal<>();
    private static final ThreadLocal<String>    browserVersionHolder = new ThreadLocal<>();

    private DriverManager() {}

    public static void initDriver(String browserName) {
        String browser = (browserName != null && !browserName.isBlank())
                ? browserName.trim().toLowerCase()
                : ConfigReader.getBrowser().toLowerCase();

        boolean headless = ConfigReader.isHeadless();
        WebDriver driver;

        switch (browser) {
            case "firefox":
                FirefoxOptions ffOpts = new FirefoxOptions();
                if (headless) ffOpts.addArguments("--headless");
                driver = new FirefoxDriver(ffOpts);
                break;

            case "edge":
                EdgeOptions edgeOpts = new EdgeOptions();
                if (headless) edgeOpts.addArguments("--headless=new");
                driver = new EdgeDriver(edgeOpts);
                browser = "edge";
                break;

            default: // chrome
                ChromeOptions chromeOpts = new ChromeOptions();
                if (headless) {
                    chromeOpts.addArguments("--headless=new");
                    chromeOpts.addArguments("--no-sandbox");           // required in Docker / CI
                    chromeOpts.addArguments("--disable-dev-shm-usage"); // avoids /dev/shm OOM in containers
                }
                chromeOpts.addArguments("--disable-gpu", "--window-size=1920,1080");
                driver = new ChromeDriver(chromeOpts);
                browser = "chrome";
        }

        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getImplicitWait()));

        Capabilities caps = ((RemoteWebDriver) driver).getCapabilities();
        String version = caps.getBrowserVersion();
        if (version == null || version.isEmpty()) {
            Object v = caps.getCapability("browserVersion");
            version = v != null ? v.toString() : "unknown";
        }

        driverHolder.set(driver);
        browserHolder.set(browser);
        browserVersionHolder.set(version);
    }

    public static boolean isInitialized() {
        return driverHolder.get() != null;
    }

    public static WebDriver getDriver() {
        WebDriver driver = driverHolder.get();
        if (driver == null) throw new IllegalStateException("WebDriver not initialised for this thread");
        return driver;
    }

    public static String getBrowserName()    { return browserHolder.get(); }
    public static String getBrowserVersion() { return browserVersionHolder.get(); }

    public static void quitDriver() {
        WebDriver driver = driverHolder.get();
        if (driver != null) {
            driver.quit();
            driverHolder.remove();
            browserHolder.remove();
            browserVersionHolder.remove();
        }
    }
}
