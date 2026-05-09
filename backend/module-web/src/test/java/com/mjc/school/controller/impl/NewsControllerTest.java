package com.mjc.school.controller.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("News controller RestAssured integration tests")
class NewsControllerTest extends BaseControllerTest{

    @Test
    @DisplayName("GET /news with pagination - Should return 200 with correct page")
    void getAllNewsWithPagination_ShouldReturn200(){
        given()
                .spec(requestSpecification)
                .queryParam("page",1)
                .queryParam("pageSize",5)
        .when()
                .get("/news")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("currentPage",equalTo(1))
                .body("modelDtoList.size()",lessThanOrEqualTo(5));
    }

    @Test
    @DisplayName("POST /news - Should return 201 and create news")
    void createNews_ShouldReturn201(){
        String newsJson = """
                {
                    "title":"RestAssured",
                    "content":"Testing framework",
                    "imageUrl": "https://example.com/image.jpg",
                    "sourceIcon": "https://example.com/icon.png",
                    "author":"Gosling",
                    "tags": ["Technology"],
                    "commentsIds":[]
                }
                """;

        given()
                .spec(requestSpecification)
                .body(newsJson)
        .when()
                .post("/news")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id",notNullValue())
                .body("title",equalTo("RestAssured"))
                .body("createdDate",notNullValue())
                .body("_links.self.href",notNullValue())
                .body("_links.update.href",notNullValue())
                .body("_links.delete.href",notNullValue());
    }


    @Test
    @DisplayName("POST /news with invalid data - Should return 400")
    void createNewsWithInvalidData_ShouldReturn400(){
        String newsJson = """
                {
                    "title":"Re",
                    "content":"Te",
                    "imageUrl": "https://example.com/image.jpg",
                    "sourceIcon": "https://example.com/icon.png",
                    "author":"Gosling",
                    "tags": ["Technology"],
                    "commentsIds":[]
                }
                """;

        given()
                .spec(requestSpecification)
                .body(newsJson)
        .when()
                .post("/news")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("code",notNullValue())
                .body("message",containsString("Validation failed"));
    }


    @Test
    @DisplayName("GET /news/{id} - Should return 200 and news details")
    void getNewsById_WhenExists_ShouldReturn200(){
        String newsJson = """
                {
                    "title":"RestAssured",
                    "content":"Testing framework",
                    "imageUrl": "https://example.com/image.jpg",
                    "sourceIcon": "https://example.com/icon.png",
                    "author":"Gosling",
                    "tags": ["Technology"],
                    "commentsIds":[]
                }
                """;

        Integer newsId =
                given()
                        .spec(requestSpecification)
                        .body(newsJson)
                        .when()
                        .post("/news")
                        .then()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract()
                        .path("id");

        given()
                .spec(requestSpecification)
                .when()
                .get("/news/" + newsId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id",equalTo(newsId))
                .body("title",equalTo("RestAssured"));
    }


    @Test
    @DisplayName("GET /news/{id} - Should return 404 when news not found")
    void getNewsById_WhenNotExists_ShouldReturn404(){
        given()
                .spec(requestSpecification)
                .when()
                .get("/news/9999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("code",notNullValue())
                .body("message",containsString("Resource not found"));
    }


    @Test
    @DisplayName("PATCH /news/{id} - Should return 200 and update news")
    void updateNews_WhenExists_ShouldReturn200(){

        String newsJson = """
                {
                    "title":"RestAssured",
                    "content":"Testing framework",
                    "imageUrl": "https://example.com/image.jpg",
                    "sourceIcon": "https://example.com/icon.png",
                    "author":"Gosling",
                    "tags": ["Technology"],
                    "commentsIds":[]
                }
                """;

        Integer newsId =
                given()
                        .spec(requestSpecification)
                        .body(newsJson)
                        .when()
                        .post("/news")
                        .then()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract()
                        .path("id");

        String updateJson = """
                {
                    "title":"Rest Updated",
                    "content":"Testing Updated",
                    "imageUrl": "https://example.com/image.jpg",
                    "sourceIcon": "https://example.com/icon.png",
                    "author":"Johnson",
                    "tags": ["Science"],
                    "commentsIds":[]
                }
                """;

        given()
                .spec(requestSpecification)
                .body(updateJson)
                .when()
                .patch("/news/"+newsId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id",equalTo(newsId))
                .body("title",equalTo("Rest Updated"))
                .body("content",equalTo("Testing Updated"))
                .body("lastUpdatedDate",notNullValue());
    }


    @Test
    @DisplayName("PATCH /news/{id} - Should return 404 when updating non-existent news")
    void updateNews_WhenNotExists_ShouldReturn404(){
        String newsJson = """
                {
                    "title":"RestAssured",
                    "content":"Testing framework",
                    "imageUrl": "https://example.com/image.jpg",
                    "sourceIcon": "https://example.com/icon.png",
                    "author":"Gosling",
                    "tags": ["Technology"],
                    "commentsIds":[]
                }
                """;
        given()
                .spec(requestSpecification)
                .body(newsJson)
        .when()
                .patch("/news/9999")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }


    @Test
    @DisplayName("DELETE /news/{id} - Should return 204 when deleting existing news")
    void deleteNews_WhenExists_ShouldReturn204(){
        String toBeDeletedJson = """
                {
                    "title":"RestAssured",
                    "content":"Testing framework",
                    "imageUrl": "https://example.com/image.jpg",
                    "sourceIcon": "https://example.com/icon.png",
                    "author":"Gosling",
                    "tags": ["Technology"],
                    "commentsIds":[]
                }
                """;

        Integer newsId =
                given()
                        .spec(requestSpecification)
                        .body(toBeDeletedJson)
                .when()
                        .post("/news")
                .then()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract()
                        .path("id");

        given()
                .spec(requestSpecification)
        .when()
                .delete("/news/"+newsId)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .spec(requestSpecification)
        .when()
                .get("/news/" + newsId)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }


    @Test
    @DisplayName("DELETE /news/{id} - Should return 404 when deleting non-existent news")
    void deleteAuthor_whenNotExists_ShouldReturn404(){

        given()
                .spec(requestSpecification)
        .when()
                .delete("/news/9999")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("GET /news/{id}/tags - Should return 200 and list of tags")
    void getNewsTags_ShouldReturn200(){
        String newsJson = """
                {
                    "title":"RestAssured",
                    "content":"Testing framework",
                    "imageUrl": "https://example.com/image.jpg",
                    "sourceIcon": "https://example.com/icon.png",
                    "author":"Gosling",
                    "tags": ["Technology","Science"],
                    "commentsIds":[]
                }
                """;

        Integer newsId =
                given()
                        .spec(requestSpecification)
                        .body(newsJson)
                        .when()
                        .post("/news")
                        .then()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract()
                        .path("id");

        given()
                .spec(requestSpecification)
                .when()
                .get("/news/" +newsId+"/tags")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$",hasSize(2))
                .body("name",hasItems("Technology","Science"));
    }

    @Test
    @DisplayName("GET /news/{id}/author - Should return 200 and author details")
    void getNewsAuthor_ShouldReturn200(){
        String newsJson = """
                {
                    "title":"RestAssured",
                    "content":"Testing framework",
                    "imageUrl": "https://example.com/image.jpg",
                    "sourceIcon": "https://example.com/icon.png",
                    "author":"Gosling",
                    "tags": ["Technology","Science"],
                    "commentsIds":[]
                }
                """;

        Integer newsId =
                given()
                        .spec(requestSpecification)
                        .body(newsJson)
                        .when()
                        .post("/news")
                        .then()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract()
                        .path("id");

        given()
                .spec(requestSpecification)
                .when()
                .get("/news/" +newsId+"/author")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("name",equalTo("Gosling"))
                .body("createdDate",notNullValue());
    }

    @Test
    @DisplayName("GET /news/{id}/comments - Should return 200 and list of comments")
    void getNewsComments_ShouldReturn200() throws JSONException {
        String newsJson = """
                {
                    "title":"RestAssured",
                    "content":"Testing framework",
                    "imageUrl": "https://example.com/image.jpg",
                    "sourceIcon": "https://example.com/icon.png",
                    "author":"Gosling",
                    "tags": ["Technology","Science"],
                    "commentsIds":[]
                }
                """;

        Integer newsId =
                given()
                        .spec(requestSpecification)
                        .body(newsJson)
                        .when()
                        .post("/news")
                        .then()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract()
                        .path("id");

        Long longNewsId = Long.valueOf(newsId);
        JSONObject commentJson = new JSONObject();
        commentJson.put("content","Great Tool");
        commentJson.put("newsId",longNewsId);

        given()
                .spec(requestSpecification)
                .body(commentJson.toString())
                .when()
                .post("/comments")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        given()
                .spec(requestSpecification)
                .when()
                .get("/news/" +newsId+"/comments")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$",hasSize(greaterThanOrEqualTo(1)))
                .body("[0].content",equalTo("Great Tool"))
                .body("[0].newsId",equalTo(newsId));
    }
}
