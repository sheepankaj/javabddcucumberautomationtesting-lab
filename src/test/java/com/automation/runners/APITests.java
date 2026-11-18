package com.automation.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features  = "src/test/resources/features/api",
        glue      = {"com.automation.stepdefinitions", "com.automation.hooks"},
        plugin    = {
                "pretty",
                "html:target/cucumber-reports/api/cucumber-report.html",
                "json:target/cucumber-reports/api/cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        tags      = "@api",
        monochrome = true
)
public class APITests {}
