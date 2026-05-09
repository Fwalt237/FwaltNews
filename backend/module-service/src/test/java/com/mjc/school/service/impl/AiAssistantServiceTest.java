package com.mjc.school.service.impl;

import com.mjc.school.repository.airepo.impl.ChatMessagesRepository;
import com.mjc.school.repository.airepo.impl.NewsEmbeddingsRepository;
import com.mjc.school.repository.airepo.model.ChatMessages;
import com.mjc.school.repository.impl.AuthorRepository;
import com.mjc.school.repository.impl.NewsRepository;
import com.mjc.school.repository.impl.TagRepository;
import com.mjc.school.repository.model.Author;
import com.mjc.school.repository.model.News;
import com.mjc.school.repository.model.Tag;
import com.mjc.school.service.aiservice.AiAssistantService;
import com.mjc.school.service.aiservice.EmbeddingService;
import com.mjc.school.service.aiservice.MyEmbeddingClient;
import com.mjc.school.service.aiservice.NewsTools;
import com.mjc.school.service.aiservice.dto.NewsSearchResult;
import com.mjc.school.service.fetcher.NewsFetcherService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

@SpringBootTest(
        classes = AiAssistantServiceTest.TestConfig.class,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "spring.jpa.hibernate.ddl-auto=create",
                "spring.flyway.enabled=false",
                "jwt.secret=dummy_test_secret_key_that_is_at_least_256_bits_long_for_hmac",
                "jwt.expiration=3600000"
        }
)
@Testcontainers
@Transactional
@Commit
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("AI Assistant Tests")
class AiAssistantServiceTest {
    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("pgvector/pgvector:pg17"))
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
                    .withInitScript("init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username",  postgres::getUsername);
        registry.add("spring.datasource.password",  postgres::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.ai.google.genai.api-key", () -> "test-key");
    }

    @MockBean
    private MyEmbeddingClient embeddingClient;

    @MockBean
    private NewsFetcherService newsFetcherService;

    @Autowired private EmbeddingService embeddingService;
    @Autowired private NewsTools newsTools;
    @Autowired private AiAssistantService assistantService;
    @Autowired private NewsEmbeddingsRepository embeddingRepository;
    @Autowired private ChatMessagesRepository chatMessagesRepository;
    @Autowired private NewsRepository newsRepository;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private ApplicationContext applicationContext;

    private static Long techNewsId;
    private static Long climateNewsId;

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            FlywayAutoConfiguration.class,
            SecurityAutoConfiguration.class,
            OAuth2ClientAutoConfiguration.class
    })
    @EnableJpaRepositories(basePackages = "com.mjc.school.repository")
    @EntityScan(basePackages = {
            "com.mjc.school.repository.model",
            "com.mjc.school.repository.airepo.model"
    })
    @ComponentScan(basePackages = "com.mjc.school.service")
    @TestConfiguration
    @EnableWebSecurity
    @ImportAutoConfiguration(SecurityAutoConfiguration.class)
    static class TestConfig {
        @Bean
        public ChatClient.Builder chatClientBuilder() {
            ChatClient mockClient = mock(ChatClient.class);
            ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
            ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);

            when(mockClient.prompt()).thenReturn(requestSpec);
            when(requestSpec.system(anyString())).thenReturn(requestSpec);
            when(requestSpec.messages(anyList())).thenReturn(requestSpec);
            when(requestSpec.call()).thenReturn(callSpec);
            when(callSpec.content()).thenReturn(
                    "Here are the latest articles. ARTICLES_FOUND:[1]");

            ChatClient.Builder builder = mock(ChatClient.Builder.class);
            when(builder.defaultTools(any())).thenReturn(builder);
            when(builder.build()).thenReturn(mockClient);
            return builder;
        }

        @Bean
        public ChatModel chatModel() {
            return Mockito.mock(ChatModel.class);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @BeforeEach
    void setUp() {
        float[] mockVector = new float[768];
        Arrays.fill(mockVector, 0.1f);
        when(embeddingClient.getEmbedding(anyString())).thenReturn(mockVector);
    }

    @Test
    @Order(1)
    @DisplayName("pgvector container and schema migration should succeed")
    void pgvectorContainerStarts() {
        assertThat(postgres.isRunning()).isTrue();
        assertThat(embeddingRepository.count()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @Order(2)
    @DisplayName("EmbeddingService should save vector embedding for news article")
    void embeddingService_embedsAndPersistsVector() {
        Author author = new Author();
        author.setName("Tech Author");
        author = authorRepository.save(author);

        Tag tag = new Tag(); tag.setName("tech");
        tag = tagRepository.save(tag);

        News news = new News();
        news.setTitle("Breakthrough in Quantum Computing Announced");
        news.setContent("Researchers have achieved a major milestone in quantum processing...");
        news.setAuthor(author);
        news.setTags(List.of(tag));
        news = newsRepository.save(news);
        techNewsId = news.getId();

        embeddingService.embedNews(news.getId());
        assertThat(embeddingRepository.existsByNewsId(techNewsId)).isTrue();
        float[] stored = embeddingRepository.findByNewsId(techNewsId)
                .orElseThrow().getEmbedding();
        assertThat(stored).hasSize(768);
        assertThat(stored[0]).isEqualTo(0.1f, offset(0.01f));
    }

    @Test
    @Order(3)
    @DisplayName("EmbedMissing should not duplicate existing embeddings")
    void embeddingService_embedMissingSkipsDuplicates() {
        long before = embeddingRepository.count();
        embeddingService.embedMissing();
        long after  = embeddingRepository.count();
        assertThat(after).isGreaterThanOrEqualTo(before);
        assertThat(embeddingRepository.findAll().stream()
                .filter(e -> e.getNews().getId().equals(techNewsId))
                .count()).isEqualTo(1);
    }

    @Test
    @Order(4)
    @DisplayName("SearchNewsByTopic should find semantically similar articles")
    void newsTools_searchByTopic_returnsResults() {
        Author author = new Author();
        author.setName("Climate Author");
        author = authorRepository.saveAndFlush(author);

        Tag tag = new Tag();
        tag.setName("climate");
        tag = tagRepository.saveAndFlush(tag);

        News climate = new News();
        climate.setTitle("Record Temperatures Hit Global Cities");
        climate.setContent("Climate scientists warn of unprecedented heat waves spreading globally...");
        climate.setAuthor(author);
        climate.setTags(List.of(tag));
        climate.setCreatedDate(LocalDateTime.now());
        climate = newsRepository.saveAndFlush(climate);

        climateNewsId = climate.getId();
        embeddingService.embedNews(climateNewsId);

        embeddingRepository.flush();

        NewsSearchResult result = newsTools.searchNewsByTopic("Record Temperatures Hit Global Cities", 1, 5);
        assertThat(result.totalFound()).isGreaterThan(0);
        assertThat(result.articles()).isNotEmpty();
        result.articles().forEach(item -> assertThat(item.title()).isNotBlank());
    }

    @Test
    @Order(5)
    @DisplayName("GetLatestNewsByTag should filter correctly by tag name")
    void newsTools_getLatestByTag_filtersCorrectly() {
        NewsSearchResult techResult    = newsTools.getLatestNewsByTag("tech",    0, 5);
        NewsSearchResult climateResult = newsTools.getLatestNewsByTag("climate", 0, 5);

        assertThat(techResult.articles()).anyMatch(a -> a.tags().contains("tech"));
        assertThat(climateResult.articles()).anyMatch(a -> a.tags().contains("climate"));
    }

    @Test
    @Order(6)
    @DisplayName("GetTopRecentNews should return articles within time window")
    void newsTools_getTopRecentNews_respectsTimeWindow() {
        Tag tag = tagRepository.findByName("climate")
                .orElseGet(() -> tagRepository.save(new Tag("climate")));
        News recentNews = new News();
        recentNews.setTitle("Recent Event");
        recentNews.setContent("Something happened just now.");
        recentNews.setAuthor(authorRepository.findAll().get(0));

        recentNews.setCreatedDate(LocalDateTime.now());
        recentNews.setTags(List.of(tag));
        newsRepository.saveAndFlush(recentNews);

        NewsSearchResult result = newsTools.getTopRecentNews(1, 5);

        assertThat(result.articles()).isNotEmpty();
        result.articles().forEach(a -> assertThat(a.publishedAt()).isNotBlank());
    }

    @Test
    @Order(7)
    @DisplayName("Chat history should be persisted and retrieved per session")
    void chatHistory_persistedAndRetrieved() {

        String sessionId = "test-session-" + System.currentTimeMillis();

        ChatMessages userMsg = new ChatMessages();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent("What is the latest tech news?");

        ChatMessages aiMsg = new ChatMessages();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("assistant");
        aiMsg.setContent("Here are the latest tech articles...");

        chatMessagesRepository.save(userMsg);
        chatMessagesRepository.save(aiMsg);

        var history = assistantService.getHistory(sessionId);
        assertThat(history).hasSize(2);
        assertThat(history.get(0).role()).isEqualTo("user");
        assertThat(history.get(1).role()).isEqualTo("assistant");

        assistantService.clearHistory(sessionId);
        assertThat(assistantService.getHistory(sessionId)).isEmpty();
    }
}
