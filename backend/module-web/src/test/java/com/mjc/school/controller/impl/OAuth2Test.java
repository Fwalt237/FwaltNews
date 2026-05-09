package com.mjc.school.controller.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "spring.security.oauth2.client.registration.google.client-id=mock-google-id",
                "spring.security.oauth2.client.registration.google.client-secret=mock-google-secret",
                "spring.security.oauth2.client.registration.google.scope=openid,profile,email",
                "spring.security.oauth2.client.registration.github.client-id=mock-github-id",
                "spring.security.oauth2.client.registration.github.client-secret=mock-github-secret",
                "spring.security.oauth2.client.registration.github.scope=read:user,user:email",
                "spring.ai.google.genai.api-key=mock-api-key",
                "spring.flyway.enabled=false",
                "spring.jpa.hibernate.ddl-auto=create",
                "jwt.secret=YWxsYmVpbmduaWNlYW5kbG9uZ2Vub3VnaHRvc2F0aXNmeWhtYWNzaGEx",
                "jwt.expiration=3600000"

        }
)
@AutoConfigureMockMvc
@EntityScan("com.mjc.school.repository.airepo.model")
@EnableJpaRepositories("com.mjc.school.repository")
@Testcontainers
@DisplayName("OAuth2 integration tests")
class OAuth2Test {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg17")
    )
            .withDatabaseName("oauthtest")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username",  postgres::getUsername);
        registry.add("spring.datasource.password",  postgres::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @MockBean
    ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("OAuth2 with Google — SuccessHandler redirects with Location header")
    void oauth2GoogleLogin_ShouldRedirectToSuccessHandler() throws Exception {
        mockMvc.perform(get("/login/oauth2/code/google")
                        .with(oauth2Login()
                                .attributes(attrs -> {
                                    attrs.put("sub",   "123456");
                                    attrs.put("name",  "Rod Johnson");
                                    attrs.put("email", "rod@gmail.com");
                                })
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        ))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("OAuth2 with GitHub — SuccessHandler redirects with Location header")
    void oauth2GithubLogin_ShouldRedirectToSuccessHandler() throws Exception {
        mockMvc.perform(get("/login/oauth2/code/github")
                        .with(oauth2Login()
                                .attributes(attrs -> {
                                    attrs.put("id",    "123456");
                                    attrs.put("login", "Rod Johnson");
                                    attrs.put("email", "rod@github.com");
                                })
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        ))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().exists("Location"));
    }
}
