package com.mjc.school.controller.impl;

import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("Comment controller RestAssured integration tests")
class CommentControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("GET /comments with pagination - Should return 200 with correct page")
    void getAllCommentsWithPagination_ShouldReturn200(){
        given()
                .spec(requestSpecification)
                .queryParam("page",1)
                .queryParam("pageSize",5)
        .when()
                .get("/comments")
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("currentPage",equalTo(1))
                .body("modelDtoList.size()",lessThanOrEqualTo(5));
    }


    @Test
    @DisplayName("POST /comments - Should return 201 and show correct admin username")
    void createComment_ShouldReturn201() throws JSONException {
        String newsJson = """
                {
                    "title":"RestAssured",
                    "content":"Testing framework",
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
                .statusCode(HttpStatus.CREATED.value())
                .body("id",notNullValue())
                .body("content",equalTo("Great Tool"))
                .body("newsId",equalTo(newsId))
                .body("authorName", equalTo("admin"));

    }

    @Test
    @DisplayName("POST /comments as regular user - Should link to regularUser")
    void createCommentAsUser_ShouldShowRegularUserName() throws JSONException {
        String newsJson = """
                {
                    "title":"RestAssured",
                    "content":"Testing framework",
                    "author":"Gosling",
                    "tags": ["Technology"],
                    "commentsIds":[]
                }
                """;

        Integer newsId = given()
                            .spec(requestSpecification)
                            .body(newsJson).post("/news")
                        .then().extract()
                            .path("id");

        String userToken = obtainUserToken();

        JSONObject commentJson = new JSONObject();
        commentJson.put("content", "I am a regular user");
        commentJson.put("newsId", newsId);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(commentJson.toString())
        .when()
                .post("/comments")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("authorName", equalTo("regularUser"));
    }

    @Test
    @DisplayName("POST /comments with invalid data - Should return 400")
    void createCommentWithInvalidData_ShouldReturn400(){
        String invalidRequest = """
                {
                    "content":"Bad",
                    "newsId":1
                }
                """;
        given()
                .spec(requestSpecification)
                .body(invalidRequest)
        .when()
                .post("/comments")
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("code",notNullValue())
                .body("message",containsString("Validation failed"));
    }

    @Test
    @DisplayName("POST /comments with non-existent news - Should return 404")
    void createCommentWithNonExistentNews_ShouldReturn404(){
        String commentJson = """
                {
                    "content":"Non existing news",
                    "newsId":999
                }
                """;
        given()
                .spec(requestSpecification)
                .body(commentJson)
        .when()
                .post("/comments")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("GET /comments/{id} - Should return 200 and comment details")
    void getCommentById_WhenExists_ShouldReturn200() throws JSONException {
        String newsJson = """
                {
                    "title":"RestAssured",
                    "content":"Testing framework",
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

        Long longNewsId = Long.valueOf(newsId);
        JSONObject commentJson = new JSONObject();
        commentJson.put("content","Great Tool");
        commentJson.put("newsId",longNewsId);

        Integer commentId =
                given()
                        .spec(requestSpecification)
                        .body(commentJson.toString())
                .when()
                        .post("/comments")
                .then()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract()
                        .path("id");

        given()
                .spec(requestSpecification)
                .basePath("/api/v2")
        .when()
                .get("/comments/"+commentId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("content",equalTo("Great Tool"))
                .body("newsId",equalTo(newsId));
    }

    @Test
    @DisplayName("GET /comments/{id} - Should return 404 when comment not found")
    void getCommentsById_WhenNotExists_ShouldReturn404(){
        given()
                .spec(requestSpecification)
                .basePath("/api/v2")
        .when()
                .get("/comments/9999")
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("code",notNullValue())
                .body("message",containsString("Resource not found"));
    }

    @Test
    @DisplayName("PUT /comments/{id} - Should return 200 and update comment")
    void updateComment_WhenExists_ShouldReturn200() throws JSONException {
        String newsJson = """
                {
                    "title":"RestAssured",
                    "content":"Testing framework",
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

        Long longNewsId = Long.valueOf(newsId);
        JSONObject commentJson = new JSONObject();
        commentJson.put("content","Great Tool");
        commentJson.put("newsId",longNewsId);

        Integer commentId =
                given()
                        .spec(requestSpecification)
                        .body(commentJson.toString())
                .when()
                        .post("/comments")
                .then()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract()
                        .path("id");

        JSONObject updatedCommentJson = new JSONObject();
        updatedCommentJson.put("content","Updated Tool");
        updatedCommentJson.put("newsId",longNewsId);

        given()
                .spec(requestSpecification)
                .body(updatedCommentJson.toString())
        .when()
                .put("/comments/"+commentId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("content",equalTo("Updated Tool"))
                .body("newsId",equalTo(newsId));
    }

    @Test
    @DisplayName("DELETE /comments/{id} - Should return 204 when deleting existing comment")
    void deleteComment_whenExists_ShouldReturn204() throws JSONException {
        String newsJson = """
                {
                    "title":"RestAssured",
                    "content":"Testing framework",
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

        Long longNewsId = Long.valueOf(newsId);
        JSONObject commentJson = new JSONObject();
        commentJson.put("content","Great Tool");
        commentJson.put("newsId",longNewsId);

        Integer commentId =
                given()
                        .spec(requestSpecification)
                        .body(commentJson.toString())
                .when()
                        .post("/comments")
                .then()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract()
                        .path("id");

        given()
                .spec(requestSpecification)
        .when()
                .delete("/comments/"+commentId)
        .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .spec(requestSpecification)
                .basePath("/api/v2")
        .when()
                .get("/comments/"+commentId)
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

    }
}
