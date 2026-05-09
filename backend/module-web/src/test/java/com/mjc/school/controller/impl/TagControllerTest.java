package com.mjc.school.controller.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("Tag controller RestAssured integration tests")
class TagControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("GET /tags with pagination - Should return 200 with correct page")
    void getAllTagsWithPagination_ShouldReturn200(){
        given()
                .spec(requestSpecification)
                .queryParam("page",1)
                .queryParam("pageSize",5)
        .when()
                .get("/tags")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("currentPage",equalTo(1))
                .body("modelDtoList.size()",lessThanOrEqualTo(5));
    }


    @Test
    @DisplayName("POST /tags - Should return 201 and create tag")
    void createTag_ShouldReturn201(){
        String tagJson = """
                {
                    "name":"Technology"
                }
                """;

        given()
                .spec(requestSpecification)
                .body(tagJson)
        .when()
                .post("/tags")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id",notNullValue())
                .body("name",equalTo("Technology"))
                .body("_links.self.href",notNullValue())
                .body("_links.update.href",notNullValue())
                .body("_links.delete.href",notNullValue());
    }


    @Test
    @DisplayName("POST /tags with invalid data - Should return 400")
    void createTagWithInvalidData_ShouldReturn400(){
        String invalidRequest = """
                {
                    "name":"Te"
                }
                """;
        given()
                .spec(requestSpecification)
                .body(invalidRequest)
        .when()
                .post("/tags")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("code",notNullValue())
                .body("message",containsString("Validation failed"));
    }


    @Test
    @DisplayName("GET /tags/{id} - Should return 200 and tag details")
    void getTagById_WhenExists_ShouldReturn200(){
        String tagJson = """
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

        given()
                .spec(requestSpecification)
                .basePath("/api/v2")
        .when()
                .get("/tags/" + tagId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("id",equalTo(tagId))
                .body("name",equalTo("Technology"))
                .body("_links.self.href",containsString("/v2/tags/" + tagId));
    }


    @Test
    @DisplayName("GET /tags/{id} - Should return 404 when tag not found")
    void getTagsById_WhenNotExists_ShouldReturn404(){
        given()
                .spec(requestSpecification)
                .basePath("/api/v2")
        .when()
                .get("/tags/9999")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("code",notNullValue())
                .body("message",containsString("Resource not found"));
    }


    @Test
    @DisplayName("PATCH /tags/{id} - Should return 200 and update tag")
    void updateTag_WhenExists_ShouldReturn200(){

        String tagJson = """
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

        String updateJson = """
                {
                    "name":"Science"
                }
                """;
        given()
                .spec(requestSpecification)
                .body(updateJson)
        .when()
                .patch("/tags/"+tagId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("id",equalTo(tagId))
                .body("name",equalTo("Science"));
    }


    @Test
    @DisplayName("PATCH /tags/{id} - Should return 404 when updating non-existent tag")
    void updateTag_WhenNotExists_ShouldReturn404(){
        String updateJson = """
                {
                    "name":"Science"
                }
                """;
        given()
                .spec(requestSpecification)
                .body(updateJson)
        .when()
                .patch("/tags/9999")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }


    @Test
    @DisplayName("DELETE /tags/{id} - Should return 204 when deleting existing tag")
    void deleteTag_WhenExists_ShouldReturn204(){
        String toBeDeletedJson = """
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

        given()
                .spec(requestSpecification)
        .when()
                .delete("/tags/"+tagId)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

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
    void deleteTag_whenNotExists_ShouldReturn404(){

        given()
                .spec(requestSpecification)
        .when()
                .delete("/tags/9999")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

}
