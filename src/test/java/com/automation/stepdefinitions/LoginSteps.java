package com.automation.stepdefinitions;

import com.automation.pages.LoginPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

public class LoginSteps {

    private LoginPage loginPage;

    @Given("the user navigates to the login page")
    public void theUserNavigatesToTheLoginPage() {
        loginPage = new LoginPage();
        loginPage.navigate();
    }

    @When("the user enters username {string} and password {string}")
    public void theUserEntersCredentials(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
    }

    @And("clicks the login button")
    public void clicksTheLoginButton() {
        loginPage.clickLogin();
    }

    @Then("the user should be redirected to the inventory page")
    public void theUserShouldBeRedirectedToTheInventoryPage() {
        Assert.assertTrue("Expected redirect to inventory page after successful login",
                loginPage.isLoggedIn());
    }

    @Then("the user should see an error message {string}")
    public void theUserShouldSeeAnErrorMessage(String expectedMessage) {
        Assert.assertTrue("Error message should be visible", loginPage.isErrorDisplayed());
        Assert.assertEquals("Error message text mismatch", expectedMessage, loginPage.getErrorMessage());
    }
}
