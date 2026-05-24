package com.mjc.school.controller.impl;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("Tag controller RestAssured integration tests")
class TagControllerTest extends BaseControllerTest {

  @Test
  @DisplayName("GET /tags with pagination - Should return 200 with correct page")
  void getAllTagsWithPagination_ShouldReturn200() {
    // Given (parameters set inline)
    // When
    given()
        .spec(requestSpecification)
        .queryParam("page", 1)
        .queryParam("pageSize", 5)
        .when()
        .get("/tags")
        // Then
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("currentPage", equalTo(1))
        .body("modelDtoList.size()", lessThanOrEqualTo(5));
  }

  @Test
  @DisplayName("POST /tags - Should return 201 and create tag")
  void createTag_ShouldReturn201() {
    // Given
    String tagJson =
        """
                    {
                        "name":"Technology"
                    }
                    """;
    // When
    given()
        .spec(requestSpecification)
        .body(tagJson)
        .when()
        .post("/tags")
        // Then
        .then()
        .statusCode(HttpStatus.CREATED.value())
        .body("id", notNullValue())
        .body("name", equalTo("Technology"))
        .body("_links.self.href", notNullValue())
        .body("_links.update.href", notNullValue())
        .body("_links.delete.href", notNullValue());
  }

  @Test
  @DisplayName("POST /tags with invalid data - Should return 400")
  void createTagWithInvalidData_ShouldReturn400() {
    // Given
    String invalidRequest =
        """
                    {
                        "name":"Te"
                    }
                    """;
    // When
    given()
        .spec(requestSpecification)
        .body(invalidRequest)
        .when()
        .post("/tags")
        // Then
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body("code", notNullValue())
        .body("message", containsString("Validation failed"));
  }

  @Test
  @DisplayName("GET /tags/{id} - Should return 200 and tag details")
  void getTagById_WhenExists_ShouldReturn200() {
    // Given
    String tagJson =
        """
                    {
                        "name":"Technology"
                    }
                    """;

    Integer tagId =
        given()
            .spec(requestSpecification)
            .body(tagJson)
            .when()
            .post("/tags")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("id");
    // When
    given()
        .spec(requestSpecification)
        .basePath("/api/v2")
        .when()
        .get("/tags/" + tagId)
        // Then
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("id", equalTo(tagId))
        .body("name", equalTo("Technology"))
        .body("_links.self.href", containsString("/v2/tags/" + tagId));
  }

  @Test
  @DisplayName("GET /tags/{id} - Should return 404 when tag not found")
  void getTagsById_WhenNotExists_ShouldReturn404() {
    // Given (non-existent ID)
    // When
    given()
        .spec(requestSpecification)
        .basePath("/api/v2")
        .when()
        .get("/tags/9999")
        // Then
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .body("code", notNullValue())
        .body("message", containsString("Resource not found"));
  }

  @Test
  @DisplayName("PATCH /tags/{id} - Should return 200 and update tag")
  void updateTag_WhenExists_ShouldReturn200() {
    // Given
    String tagJson =
        """
                    {
                        "name":"Technology"
                    }
                    """;
    Integer tagId =
        given()
            .spec(requestSpecification)
            .body(tagJson)
            .when()
            .post("/tags")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("id");

    String updateJson =
        """
                    {
                        "name":"Science"
                    }
                    """;
    // When
    given()
        .spec(requestSpecification)
        .body(updateJson)
        .when()
        .patch("/tags/" + tagId)
        // Then
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("id", equalTo(tagId))
        .body("name", equalTo("Science"));
  }

  @Test
  @DisplayName("PATCH /tags/{id} - Should return 404 when updating non-existent tag")
  void updateTag_WhenNotExists_ShouldReturn404() {
    // Given
    String updateJson =
        """
                    {
                        "name":"Science"
                    }
                    """;
    // When
    given()
        .spec(requestSpecification)
        .body(updateJson)
        .when()
        .patch("/tags/9999")
        // Then
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("DELETE /tags/{id} - Should return 204 when deleting existing tag")
  void deleteTag_WhenExists_ShouldReturn204() {
    // Given
    String toBeDeletedJson =
        """
                    {
                        "name":"To be deleted"
                    }
                    """;

    Integer tagId =
        given()
            .spec(requestSpecification)
            .body(toBeDeletedJson)
            .when()
            .post("/tags")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("id");
    // When
    given()
        .spec(requestSpecification)
        .when()
        .delete("/tags/" + tagId)
        // Then
        .then()
        .statusCode(HttpStatus.NO_CONTENT.value());

    // Verify deletion
    given()
        .spec(requestSpecification)
        .basePath("/api/v2")
        .when()
        .get("/tags/" + tagId)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("DELETE /tags/{id} - Should return 404 when deleting non-existent tag")
  void deleteTag_whenNotExists_ShouldReturn404() {
    // Given (non-existent ID)
    // When
    given()
        .spec(requestSpecification)
        .when()
        .delete("/tags/9999")
        // Then
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }
}
