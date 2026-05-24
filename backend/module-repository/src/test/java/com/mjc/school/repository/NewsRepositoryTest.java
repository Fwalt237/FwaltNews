package com.mjc.school.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.mjc.school.repository.impl.AuthorRepository;
import com.mjc.school.repository.impl.NewsRepository;
import com.mjc.school.repository.impl.TagRepository;
import com.mjc.school.repository.model.Author;
import com.mjc.school.repository.model.News;
import com.mjc.school.repository.model.Tag;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DisplayName("NewsRepository integration tests")
class NewsRepositoryTest extends BaseRepositoryTest {

  @Autowired private NewsRepository newsRepository;
  @Autowired private AuthorRepository authorRepository;
  @Autowired private TagRepository tagRepository;

  private Author savedAuthor;
  private Tag tagTech;
  private Tag tagClimate;
  private News savedNews;

  @BeforeEach
  void setUp() {
    newsRepository.deleteAll();
    tagRepository.deleteAll();
    authorRepository.deleteAll();

    savedAuthor = new Author();
    savedAuthor.setName("Jane Reporter");
    authorRepository.save(savedAuthor);

    tagTech = new Tag();
    tagTech.setName("technology");
    tagClimate = new Tag();
    tagClimate.setName("climate");
    tagRepository.saveAll(List.of(tagTech, tagClimate));

    savedNews = new News();
    savedNews.setTitle("Spring Boot 3 Released Today");
    savedNews.setContent("The Spring team announced the release of Spring Boot 3...");
    savedNews.setAuthor(savedAuthor);
    savedNews.setTags(new ArrayList<>(List.of(tagTech)));
    savedNews = newsRepository.save(savedNews);
  }

  @Test
  @DisplayName("save should persist news with author and tags")
  void save_persistsNewsWithAuthorAndTags() {
    // Given
    News newNews = new News();
    newNews.setTitle("New Article");
    newNews.setContent("Content...");
    newNews.setAuthor(savedAuthor);
    newNews.setTags(List.of(tagTech));

    // When
    News saved = newsRepository.save(newNews);

    // Then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getTitle()).isEqualTo("New Article");
    assertThat(saved.getCreatedDate()).isNotNull();
    assertThat(saved.getAuthor().getName()).isEqualTo("Jane Reporter");
    assertThat(saved.getTags()).hasSize(1);
    assertThat(saved.getTags().get(0).getName()).isEqualTo("technology");
  }

  @Test
  @DisplayName("findById should return news when it exists")
  void findById_returnsNewsWhenExists() {
    // Given
    Long existingId = savedNews.getId();

    // When
    Optional<News> found = newsRepository.findById(existingId);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getTitle()).isEqualTo("Spring Boot 3 Released Today");
  }

  @Test
  @DisplayName("findById should return empty for non-existent news")
  void findById_returnsEmptyForNonExistentNews() {
    // Given
    Long nonExistentId = 999L;

    // When
    Optional<News> found = newsRepository.findById(nonExistentId);

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("findAll with pagination should return correct page")
  void findAll_withPagination_returnsCorrectPage() {
    // Given
    News second = new News();
    second.setTitle("Climate Summit Conclusions Here");
    second.setContent("World leaders agreed on new climate targets...");
    second.setAuthor(savedAuthor);
    second.setTags(List.of(tagClimate));
    newsRepository.save(second);

    // When
    Page<News> page =
        newsRepository.findAll(PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "title")));

    // Then
    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
    assertThat(page.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("deleteById should remove news and cascades to newstags")
  void deleteById_removesNewsAndNewsTags() {
    // Given
    Long existingId = savedNews.getId();

    // When
    newsRepository.deleteById(existingId);

    // Then
    assertThat(newsRepository.findById(existingId)).isEmpty();
  }

  @Test
  @DisplayName("existsById should return true for existing news")
  void existsById_returnsTrueForExistingNews() {
    // Given
    Long existingId = savedNews.getId();

    // When
    boolean exists = newsRepository.existsById(existingId);

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("existsById should return false for non-existent news")
  void existsById_returnsFalseForNonExistentNews() {
    // Given
    Long nonExistentId = 999L;

    // When
    boolean exists = newsRepository.existsById(nonExistentId);

    // Then
    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("findByTagName should return news filtered by tag")
  void findByTagName_returnsNewsFilteredByTag() {
    // Given
    News climateNews = new News();
    climateNews.setTitle("Global Warming Accelerates Speed");
    climateNews.setContent("New data shows acceleration in warming...");
    climateNews.setAuthor(savedAuthor);
    climateNews.setTags(List.of(tagClimate));
    newsRepository.save(climateNews);

    // When
    List<News> techNews =
        newsRepository.findByTagName(
            "technology", PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate")));
    List<News> climateNewsList =
        newsRepository.findByTagName(
            "climate", PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate")));

    // Then
    assertThat(techNews).extracting(News::getTitle).contains("Spring Boot 3 Released Today");
    assertThat(climateNewsList)
        .extracting(News::getTitle)
        .contains("Global Warming Accelerates Speed");
    assertThat(climateNewsList)
        .extracting(News::getTitle)
        .doesNotContain("Spring Boot 3 Released Today");
  }

  @Test
  @DisplayName("findByTagNameAndCreatedDateAfter should filter by tag and time window")
  void findByTagNameAndCreatedDateAfter_filtersByTagAndTime() {
    // Given
    LocalDateTime since = LocalDateTime.now().minusMinutes(5);

    // When
    List<News> results =
        newsRepository.findByTagNameAndCreatedDateAfter(
            "technology",
            since,
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate")));

    // Then
    assertThat(results).isNotEmpty();
    assertThat(results).extracting(News::getTitle).contains("Spring Boot 3 Released Today");
  }

  @Test
  @DisplayName("findByTagNameAndCreatedDateAfter should return empty when nothing is within window")
  void findByTagNameAndCreatedDateAfter_returnsEmptyWhenNothingInWindow() {
    // Given
    LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

    // When
    List<News> results =
        newsRepository.findByTagNameAndCreatedDateAfter(
            "technology",
            futureDate,
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate")));

    // Then
    assertThat(results).isEmpty();
  }

  @Test
  @DisplayName("findByCreatedDateAfterOrderByCreatedDateDesc should return recent news")
  void findByCreatedDateAfter_returnsRecentNews() {
    // Given
    LocalDateTime since = LocalDateTime.now().minusMinutes(5);

    // When
    List<News> results =
        newsRepository.findByCreatedDateAfterOrderByCreatedDateDesc(since, PageRequest.of(0, 10));

    // Then
    assertThat(results).isNotEmpty();
    assertThat(results.get(0).getCreatedDate()).isAfter(since);
  }

  @Test
  @DisplayName("findByCreatedDateBefore should return news older than cutoff")
  void findByCreatedDateBefore_returnsOldNews() {
    // Given
    LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);

    // When
    List<News> results = newsRepository.findByCreatedDateBefore(tomorrow);

    // Then
    assertThat(results).isNotEmpty();
    assertThat(results).extracting(News::getTitle).contains("Spring Boot 3 Released Today");
  }

  @Test
  @DisplayName("findByCreatedDateBefore — returns empty when cutoff is in the past")
  void findByCreatedDateBefore_returnsEmptyWhenCutoffIsBeforeAllNews() {
    // Given
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

    // When
    List<News> results = newsRepository.findByCreatedDateBefore(yesterday);

    // Then
    assertThat(results).isEmpty();
  }

  @Test
  @DisplayName("news with multiple tags — all tags are persisted correctly")
  void newsWithMultipleTags_allTagsPersistedCorrectly() {
    // Given
    News multi = new News();
    multi.setTitle("Tech and Climate Crossroads Today");
    multi.setContent("How technology helps fight climate change...");
    multi.setAuthor(savedAuthor);
    multi.setTags(List.of(tagTech, tagClimate));

    // When
    News saved = newsRepository.save(multi);

    // Then
    News found = newsRepository.findById(saved.getId()).orElseThrow();
    assertThat(found.getTags()).hasSize(2);
    assertThat(found.getTags())
        .extracting(Tag::getName)
        .containsExactlyInAnyOrder("technology", "climate");
  }
}
