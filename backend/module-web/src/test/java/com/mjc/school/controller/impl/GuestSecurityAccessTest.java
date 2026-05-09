package com.mjc.school.controller.impl;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

@DisplayName("Guest Security Access Tests - Role Restrictions")
class GuestSecurityAccessTest extends BaseControllerTest{

    @Test
    @DisplayName("Guest Access - POST /authors should be blocked (403/401)")
    void guest_ShouldBeBlocked_WhenCreatingAuthor() {
        String authorJson = "{\"name\":\"Unauthorized\"}";

        given()
                .port(port)
                .basePath("/api/v1")
                .contentType(ContentType.JSON)
                .body(authorJson)
        .when()
                .post("/authors")
        .then()
                .statusCode(anyOf(
                        is(HttpStatus.UNAUTHORIZED.value()),
                        is(HttpStatus.FORBIDDEN.value())
                ));
    }

    @Test
    @DisplayName("Guest Access - DELETE /news should be blocked (403/401)")
    void guest_ShouldBeBlocked_WhenDeletingNews() {
        given()
                .port(port)
                .basePath("/api/v1")
        .when()
                .delete("/news/1")
        .then()
                .statusCode(anyOf(
                        is(HttpStatus.UNAUTHORIZED.value()),
                        is(HttpStatus.FORBIDDEN.value())
                ));
    }

    @Test
    @DisplayName("Guest Access - GET /news should be allowed (200)")
    void guest_ShouldBeAllowed_ToReadNews() {
        given()
                .port(port)
                .basePath("/api/v1")
        .when()
                .get("/news")
        .then()
                .statusCode(HttpStatus.OK.value());
    }
}
