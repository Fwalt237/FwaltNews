package com.mjc.school.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.mjc.school.repository.impl.AuthorRepository;
import com.mjc.school.repository.impl.NewsRepository;
import com.mjc.school.repository.impl.TagRepository;
import com.mjc.school.repository.model.Author;
import com.mjc.school.repository.model.News;
import com.mjc.school.repository.model.Tag;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DisplayName("AuthorRepository integration tests")
class AuthorRepositoryTest extends BaseRepositoryTest {

  @Autowired private AuthorRepository authorRepository;
  @Autowired private NewsRepository newsRepository;
  @Autowired private TagRepository tagRepository;

  private Author savedAuthor;

  @BeforeEach
  void setUp() {
    newsRepository.deleteAll();
    authorRepository.deleteAll();

    Author author = new Author();
    author.setName("James Gosling");
    savedAuthor = authorRepository.save(author);
  }

  @Test
  @DisplayName("Save should persist author and generate id")
  void save_persistsAuthorAndGeneratesId() {
    // Given
    Author newAuthor = new Author();
    newAuthor.setName("Robert C. Martin");

    // When
    Author saved = authorRepository.save(newAuthor);

    // Then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getName()).isEqualTo("Robert C. Martin");
    assertThat(saved.getCreatedDate()).isNotNull();
  }

  @Test
  @DisplayName("findById should return author when it exists")
  void findById_returnsAuthorWhenExists() {
    // Given
    Long existingId = savedAuthor.getId();

    // When
    Optional<Author> found = authorRepository.findById(existingId);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("James Gosling");
  }

  @Test
  @DisplayName("findById should return empty when author does not exist")
  void findById_returnsEmptyWhenNotExists() {
    // Given
    Long nonExistentId = 999L;

    // When
    Optional<Author> found = authorRepository.findById(nonExistentId);

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("findAll with pagination should return correct page size")
  void findAll_withPagination_returnsCorrectPageSize() {
    // Given
    Author secondAuthor = new Author();
    secondAuthor.setName("Rod Johnson");
    authorRepository.save(secondAuthor);

    // When
    Page<Author> page = authorRepository.findAll(PageRequest.of(0, 1, Sort.by("name")));

    // Then
    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
    assertThat(page.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("update should modify name and update lastUpdatedDate")
  void update_modifiesNameAndUpdatesLastUpdatedDate() {
    // Given
    savedAuthor.setName("Martin Fowler");

    // When
    Author updated = authorRepository.save(savedAuthor);

    // Then
    assertThat(updated.getName()).isEqualTo("Martin Fowler");
    assertThat(updated.getLastUpdatedDate()).isNotNull();
  }

  @Test
  @DisplayName("deleteById should remove author from database")
  void deleteById_removesAuthorFromDatabase() {
    // Given
    Long existingId = savedAuthor.getId();

    // When
    authorRepository.deleteById(existingId);

    // Then
    assertThat(authorRepository.findById(existingId)).isEmpty();
  }

  @Test
  @DisplayName("existsById should return true for existing author")
  void existsById_returnsTrueForExistingAuthor() {
    // Given
    Long existingId = savedAuthor.getId();

    // When
    boolean exists = authorRepository.existsById(existingId);

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("existsById should return false for non-existent author")
  void existsById_returnsFalseForNonExistentAuthor() {
    // Given
    Long nonExistentId = 999L;

    // When
    boolean exists = authorRepository.existsById(nonExistentId);

    // Then
    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("findByName should return author matching exact name")
  void findByName_returnsMatchingAuthor() {
    // Given
    String name = "James Gosling";

    // When
    Optional<Author> found = authorRepository.findByName(name);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(savedAuthor.getId());
  }

  @Test
  @DisplayName("findByName should return empty for non-existent name")
  void findByName_returnsEmptyForNonExistentName() {
    // Given
    String unknownName = "Unknown Person";

    // When
    Optional<Author> found = authorRepository.findByName(unknownName);

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("findByNewsId should return author linked to a specific news article")
  void findByNewsId_returnsLinkedAuthor() {
    // Given
    Tag tag = new Tag();
    tag.setName("java");
    tagRepository.save(tag);

    News news = new News();
    news.setTitle("Spring Framework Deep Dive");
    news.setContent("A comprehensive look at Spring internals...");
    news.setAuthor(savedAuthor);
    news.setTags(List.of(tag));
    News savedNews = newsRepository.save(news);

    // When
    Optional<Author> found = authorRepository.findByNewsId(savedNews.getId());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("James Gosling");
  }

  @Test
  @DisplayName("findByNewsId — returns empty when news does not exist")
  void findByNewsId_returnsEmptyWhenNewsNotExists() {
    // Given
    Long nonExistentNewsId = 999L;

    // When
    Optional<Author> found = authorRepository.findByNewsId(nonExistentNewsId);

    // Then
    assertThat(found).isEmpty();
  }
}
