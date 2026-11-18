package com.automation.stepdefinitions;

import com.automation.config.ConfigReader;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.junit.Assert;

import static io.restassured.RestAssured.given;

public class UserAPISteps {

    private Response response;

    @Given("I make a GET request to the users endpoint with ID {int}")
    public void iMakeAGETRequestToUsersEndpointWithId(int userId) {
        response = given()
                .baseUri(ConfigReader.getApiBaseUrl())
                .log().uri()
                .when()
                .get("/users/" + userId)
                .then()
                .log().status()
                .extract()
                .response();

        Allure.addAttachment("API Response", "application/json", response.asPrettyString(), ".json");
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatus) {
        Assert.assertEquals("HTTP status code mismatch", expectedStatus, response.statusCode());
    }

    @And("the user name should be {string}")
    public void theUserNameShouldBe(String expectedName) {
        String actual = response.jsonPath().getString("name");
        Assert.assertEquals("name field mismatch", expectedName, actual);
    }

    @And("the user username should be {string}")
    public void theUserUsernameShouldBe(String expectedUsername) {
        String actual = response.jsonPath().getString("username");
        Assert.assertEquals("username field mismatch", expectedUsername, actual);
    }

    @And("the user email should be {string}")
    public void theUserEmailShouldBe(String expectedEmail) {
        String actual = response.jsonPath().getString("email");
        Assert.assertEquals("email field mismatch", expectedEmail, actual);
    }
}
