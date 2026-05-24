package com.mjc.school.controller.impl;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("User Security Access Tests - Role Restrictions")
class UserSecurityAccessTest extends BaseControllerTest {

  private String userToken;

  @BeforeEach
  void setupUser() {
    this.userToken = obtainUserToken();
  }

  @Test
  @DisplayName("User Access - POST /comments should be ALLOWED (201)")
  void user_ShouldBeAllowed_ToPostComment() {
    // Given
    Integer newsId =
        given()
            .spec(requestSpecification)
            .body(
                "{\"title\":\"Secure News\",\"content\":\"Content of this Secure News\",\"author\":\"Gosling\",\"tags\":[],\"commentsIds\":[]}")
            .post("/news")
            .then()
            .extract()
            .path("id");

    String commentJson = String.format("{\"content\":\"Valid comment\", \"newsId\":%d}", newsId);
    // When
    given()
        .port(port)
        .basePath("/api/v1")
        .header("Authorization", "Bearer " + userToken)
        .contentType(ContentType.JSON)
        .body(commentJson)
        .when()
        .post("/comments")
        // Then
        .then()
        .statusCode(HttpStatus.CREATED.value());
  }

  @Test
  @DisplayName("User Access - DELETE /authors should be FORBIDDEN (403)")
  void user_ShouldBeBlocked_FromDeletingAuthor() {
    // Given (user token from setupUser)
    // When
    given()
        .port(port)
        .basePath("/api/v1")
        .header("Authorization", "Bearer " + userToken)
        .when()
        .delete("/authors/1")
        // Then
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value());
  }
}
