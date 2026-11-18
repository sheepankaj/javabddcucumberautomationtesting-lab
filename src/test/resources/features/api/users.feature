@api
Feature: JSONPlaceholder Users API

  @api-users
  Scenario: Validate user details for ID 2
    Given I make a GET request to the users endpoint with ID 2
    Then the response status code should be 200
    And the user name should be "Ervin Howell"
    And the user username should be "Antonette"
    And the user email should be "Shanna@melissa.tv"
