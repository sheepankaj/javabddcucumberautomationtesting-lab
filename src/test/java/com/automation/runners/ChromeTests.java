package com.automation.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features  = "src/test/resources/features/ui",
        glue      = {"com.automation.stepdefinitions", "com.automation.hooks"},
        plugin    = {
                "pretty",
                "html:target/cucumber-reports/chrome/cucumber-report.html",
                "json:target/cucumber-reports/chrome/cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        tags      = "@ui",
        monochrome = true
)
public class ChromeTests {
    static {
        System.setProperty("browser", "chrome");
    }
}
