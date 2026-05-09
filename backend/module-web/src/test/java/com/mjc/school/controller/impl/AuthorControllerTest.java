package com.mjc.school.controller.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("Author controller RestAssured integration tests")
class AuthorControllerTest extends BaseControllerTest{

    @Test
    @DisplayName("GET /authors with pagination - Should return 200 with correct page")
    void getAllAuthorsWithPagination_ShouldReturn200(){
        given()
                .spec(requestSpecification)
                .queryParam("page",1)
                .queryParam("pageSize",5)
        .when()
                .get("/authors")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("currentPage",equalTo(1))
                .body("modelDtoList.size()",lessThanOrEqualTo(5));
    }

    @Test
    @DisplayName("POST /authors - Should return 201 and create author")
    void createAuthor_ShouldReturn201(){
        String authorJson = """
                {
                    "name":"Gosling"
                }
                """;

        given()
                .spec(requestSpecification)
                .body(authorJson)
        .when()
                .post("/authors")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id",notNullValue())
                .body("name",equalTo("Gosling"))
                .body("createdDate",notNullValue())
                .body("_links.self.href",notNullValue())
                .body("_links.update.href",notNullValue())
                .body("_links.delete.href",notNullValue());
    }

    @Test
    @DisplayName("POST /authors with invalid data - Should return 400")
    void createAuthorWithInvalidData_ShouldReturn400(){
        String invalidRequest = """
                {
                    "name":"Go"
                }
                """;
        given()
                .spec(requestSpecification)
                .body(invalidRequest)
        .when()
                .post("/authors")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("code",notNullValue())
                .body("message",containsString("Validation failed"));
    }

    @Test
    @DisplayName("GET /authors/{id} - Should return 200 and author details")
    void getAuthorById_WhenExists_ShouldReturn200(){
        String authorJson = """
                {
                    "name":"Gosling"
                }
                """;

        Integer authorId =
                given()
                        .spec(requestSpecification)
                        .body(authorJson)
                .when()
                        .post("/authors")
                .then()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract()
                        .path("id");

        given()
                .spec(requestSpecification)
                .basePath("/api/v2")
        .when()
                .get("/authors/" + authorId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("id",equalTo(authorId))
                .body("name",equalTo("Gosling"))
                .body("_links.self.href",containsString("/v2/authors/" + authorId));
    }

    @Test
    @DisplayName("GET /authors/{id} - Should return 404 when author not found")
    void getAuthorsById_WhenNotExists_ShouldReturn404(){
        given()
                .spec(requestSpecification)
                .basePath("/api/v2")
        .when()
                .get("/authors/9999")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("code",notNullValue())
                .body("message",containsString("Resource not found"));
    }

    @Test
    @DisplayName("PATCH /authors/{id} - Should return 200 and update author")
    void updateAuthor_WhenExists_ShouldReturn200(){

        String authorJson = """
                {
                    "name":"Gosling"
                }
                """;
        Integer authorId =
                given()
                        .spec(requestSpecification)
                        .body(authorJson)
                .when()
                        .post("/authors")
                .then()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract()
                        .path("id");

        String updateJson = """
                {
                    "name":"Johnson"
                }
                """;
        given()
                .spec(requestSpecification)
                .body(updateJson)
        .when()
                .patch("/authors/"+authorId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("id",equalTo(authorId))
                .body("name",equalTo("Johnson"))
                .body("lastUpdatedDate",notNullValue());
    }

    @Test
    @DisplayName("PATCH /authors/{id} - Should return 404 when updating non-existent author")
    void updateAuthor_WhenNotExists_ShouldReturn404(){
        String updateJson = """
                {
                    "name":"Johnson"
                }
                """;
        given()
                .spec(requestSpecification)
                .body(updateJson)
        .when()
                .patch("/authors/9999")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("DELETE /authors/{id} - Should return 204 when deleting existing author")
    void deleteAuthor_WhenExists_ShouldReturn204(){
        String toBeDeletedJson = """
                {
                    "name":"To be deleted"
                }
                """;

        Integer authorId =
                given()
                        .spec(requestSpecification)
                        .body(toBeDeletedJson)
                .when()
                        .post("/authors")
                .then()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract()
                        .path("id");

        given()
                .spec(requestSpecification)
        .when()
                .delete("/authors/"+authorId)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .spec(requestSpecification)
                .basePath("/api/v2")
        .when()
                .get("/authors/" + authorId)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("DELETE /authors/{id} - Should return 404 when deleting non-existent author")
    void deleteAuthor_whenNotExists_ShouldReturn404(){

        given()
                .spec(requestSpecification)
        .when()
                .delete("/authors/9999")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
