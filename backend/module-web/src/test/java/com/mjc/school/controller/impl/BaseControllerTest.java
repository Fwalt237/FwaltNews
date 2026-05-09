package com.mjc.school.controller.impl;

import com.mjc.school.aicontroller.RateLimitingFilter;
import com.mjc.school.service.fetcher.NewsFetcherService;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "jwt.secret=YWxsYmVpbmduaWNlYW5kbG9uZ2Vub3VnaHRvc2F0aXNmeWhtYWNzaGEx",
                "jwt.expiration=3600000"
        }
)
@EntityScan("com.mjc.school.repository.airepo.model")
public abstract class BaseControllerTest {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg17")
    )
            .withDatabaseName("jenkinsdb")
            .withUsername("postgres")
            .withPassword("postgres");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.ai.google.genai.api-key", () -> "test-key");
    }

    @LocalServerPort
    protected int port;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @MockBean
    protected ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    protected OAuth2AuthorizedClientService authorizedClientService;

    @MockBean
    protected RateLimitingFilter rateLimitingFilter;

    @MockBean
    protected NewsFetcherService newsFetcherService;

    protected RequestSpecification requestSpecification;

    protected String adminToken;

    private static final String ADMIN_BCRYPT = "$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6";

    @BeforeEach
    void setUp() throws ServletException, IOException {

        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(rateLimitingFilter).doFilter(any(), any(), any());

        JdbcTestUtils.deleteFromTables(jdbcTemplate,"chat_messages", "news_embeddings", "newstags","comments","news", "authors", "tags","user_roles","users");

        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";

        adminToken = obtainAccessToken();

        requestSpecification = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + adminToken)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    private String obtainAccessToken() {
        jdbcTemplate.update("""
        INSERT INTO users (username, password, email, first_name, last_name,
                          enabled, account_non_expired, credentials_non_expired, account_non_locked, provider)
        VALUES ('admin', ?, 'admin@mail.com', 'Admin', 'User', true, true, true, true, 'LOCAL')
        """, ADMIN_BCRYPT);
        jdbcTemplate.update("INSERT INTO user_roles (user_id, role) VALUES ((SELECT id FROM users WHERE username = 'admin'), 'ROLE_ADMIN')");

        return given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"admin\",\"password\":\"password\"}")
                .post("/auth/login")
                .then().extract().path("token");
    }

    protected String obtainUserToken() {
        jdbcTemplate.update("""
        INSERT INTO users (username, password, email, first_name, last_name,
                          enabled, account_non_expired, credentials_non_expired, account_non_locked, provider)
        VALUES ('regularUser', ?, 'user@mail.com', 'Regular', 'User', true, true, true, true, 'LOCAL')
        """, ADMIN_BCRYPT);
        jdbcTemplate.update("INSERT INTO user_roles (user_id, role) VALUES ((SELECT id FROM users WHERE username = 'regularUser'), 'ROLE_USER')");

        return given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"regularUser\",\"password\":\"password\"}")
                .post("/auth/login")
                .then().extract().path("token");
    }

}
