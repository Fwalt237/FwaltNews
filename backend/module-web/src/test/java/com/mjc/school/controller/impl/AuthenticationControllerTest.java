package com.mjc.school.controller.impl;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("Authentication controller RestAssured integration tests")
class AuthenticationControllerTest extends BaseControllerTest {

  @Test
  @DisplayName("POST - Should return 201 and successfully create an user")
  void registerUserSuccessfully_ShouldReturn201() {
    // Given
    String signupRequest =
        """
                    {
                        "username":"rod",
                        "password":"pass",
                        "email":"rod@mail.com",
                        "firstName":"rod",
                        "lastName":"johnson"
                    }
                    """;
    // When
    given()
        .spec(requestSpecification)
        .body(signupRequest)
        .when()
        .post("/auth/signup")
        // Then
        .then()
        .statusCode(HttpStatus.CREATED.value())
        .body("token", notNullValue())
        .body("username", equalTo("rod"))
        .body("email", equalTo("rod@mail.com"));
  }

  @Test
  @DisplayName("POST - Should return 200 and successfully login an user")
  void loginUserSuccessfully_ShouldReturn200() {
    // Given
    String signupRequest =
        """
                    {
                        "username":"rod",
                        "password":"pass",
                        "email":"rod@mail.com",
                        "firstName":"rod",
                        "lastName":"johnson"
                    }
                    """;
    given().spec(requestSpecification).body(signupRequest).post("/auth/signup");

    String loginRequest =
        """
                    {
                        "username":"rod",
                        "password":"pass"
                    }
                    """;
    // When
    given()
        .spec(requestSpecification)
        .body(loginRequest)
        .when()
        .post("/auth/login")
        // Then
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("token", notNullValue())
        .body("roles", hasItem("ROLE_USER"));
  }

  @Test
  @DisplayName("POST - Should return 401 with invalid credentials")
  void failedLoginUser_ShouldReturn401() {
    // Given
    String loginRequest =
        """
                    {
                        "username":"user",
                        "password":"not_found"
                    }
                    """;
    // When
    given()
        .spec(requestSpecification)
        .body(loginRequest)
        .when()
        .post("/auth/login")
        // Then
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }
}
