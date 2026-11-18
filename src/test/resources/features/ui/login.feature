@ui
Feature: SauceDemo Login

  Background:
    Given the user navigates to the login page

  @smoke @login-success
  Scenario: Successful login with valid credentials
    When the user enters username "standard_user" and password "secret_sauce"
    And clicks the login button
    Then the user should be redirected to the inventory page

  @login-failure
  Scenario Outline: Unsuccessful login with invalid credentials
    When the user enters username "<username>" and password "<password>"
    And clicks the login button
    Then the user should see an error message "<error_message>"

    Examples:
      | username      | password       | error_message                                                             |
      | invalid_user  | secret_sauce   | Epic sadface: Username and password do not match any user in this service |
      | standard_user | wrong_password | Epic sadface: Username and password do not match any user in this service |
      |               | secret_sauce   | Epic sadface: Username is required                                        |
      | standard_user |                | Epic sadface: Password is required                                        |

  @login-locked
  Scenario: Login attempt with a locked-out user
    When the user enters username "locked_out_user" and password "secret_sauce"
    And clicks the login button
    Then the user should see an error message "Epic sadface: Sorry, this user has been locked out."
