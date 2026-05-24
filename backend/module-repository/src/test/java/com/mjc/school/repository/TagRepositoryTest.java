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

@DisplayName("TagRepository integration tests")
class TagRepositoryTest extends BaseRepositoryTest {

  @Autowired private TagRepository tagRepository;
  @Autowired private NewsRepository newsRepository;
  @Autowired private AuthorRepository authorRepository;

  private Tag savedTag;

  @BeforeEach
  void setUp() {
    newsRepository.deleteAll();
    tagRepository.deleteAll();
    authorRepository.deleteAll();

    Tag tag = new Tag();
    tag.setName("spring-boot");
    savedTag = tagRepository.save(tag);
  }

  @Test
  @DisplayName("save should persist tag and generate id")
  void save_persistsTagAndGeneratesId() {
    // Given
    Tag newTag = new Tag();
    newTag.setName("java");

    // When
    Tag saved = tagRepository.save(newTag);

    // Then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getName()).isEqualTo("java");
  }

  @Test
  @DisplayName("findById should return tag when it exists")
  void findById_returnsTagWhenExists() {
    // Given
    Long existingId = savedTag.getId();

    // When
    Optional<Tag> found = tagRepository.findById(existingId);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("spring-boot");
  }

  @Test
  @DisplayName("findById should return empty for non-existent tag")
  void findById_returnsEmptyForNonExistentTag() {
    // Given
    Long nonExistentId = 999L;

    // When
    Optional<Tag> found = tagRepository.findById(nonExistentId);

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("findAll with pagination should return sorted results")
  void findAll_withPagination_returnsSortedResults() {
    // Given
    Tag second = new Tag();
    second.setName("docker");
    Tag third = new Tag();
    third.setName("kubernetes");
    tagRepository.saveAll(List.of(second, third));

    // When
    Page<Tag> page = tagRepository.findAll(PageRequest.of(0, 10, Sort.by("name")));

    // Then
    assertThat(page.getContent()).extracting(Tag::getName).isSortedAccordingTo(String::compareTo);
  }

  @Test
  @DisplayName("update should modify tag name")
  void update_modifiesTagName() {
    // Given
    savedTag.setName("microservices");

    // When
    Tag updated = tagRepository.save(savedTag);

    // Then
    assertThat(updated.getName()).isEqualTo("microservices");
  }

  @Test
  @DisplayName("deleteById should remove tag from database")
  void deleteById_removesTag() {
    // Given
    Long existingId = savedTag.getId();

    // When
    tagRepository.deleteById(existingId);

    // Then
    assertThat(tagRepository.findById(existingId)).isEmpty();
  }

  @Test
  @DisplayName("findByName should return matching tag")
  void findByName_returnsMatchingTag() {
    // Given
    String name = "spring-boot";

    // When
    Optional<Tag> found = tagRepository.findByName(name);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(savedTag.getId());
  }

  @Test
  @DisplayName("findByName should return empty for non-existent name")
  void findByName_returnsEmptyForNonExistentName() {
    // Given
    String nonExistentName = "nonexistent";

    // When
    Optional<Tag> found = tagRepository.findByName(nonExistentName);

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("findByNewsId should return tags associated with a news article")
  void findByNewsId_returnsTagsForNews() {
    // Given
    Tag second = new Tag();
    second.setName("java");
    tagRepository.save(second);

    Author author = new Author();
    author.setName("Test Author");
    authorRepository.save(author);

    News news = new News();
    news.setTitle("Testing Spring Boot Applications");
    news.setContent("A guide to writing great Spring Boot tests...");
    news.setAuthor(author);
    news.setTags(List.of(savedTag, second));
    News saved = newsRepository.save(news);

    // When
    List<Tag> tags = tagRepository.findByNewsId(saved.getId());

    // Then
    assertThat(tags).hasSize(2);
    assertThat(tags).extracting(Tag::getName).containsExactlyInAnyOrder("spring-boot", "java");
  }

  @Test
  @DisplayName("findByNewsId should return empty list when news has no tags")
  void findByNewsId_returnsEmptyForNewsWithNoTags() {
    // Given
    Author author = new Author();
    author.setName("Author NoTag");
    authorRepository.save(author);

    News news = new News();
    news.setTitle("Untagged Article About Nothing");
    news.setContent("Content without any tags assigned to it...");
    news.setAuthor(author);
    news.setTags(List.of());
    News saved = newsRepository.save(news);

    // When
    List<Tag> tags = tagRepository.findByNewsId(saved.getId());

    // Then
    assertThat(tags).isEmpty();
  }

  @Test
  @DisplayName("findByNewsId should return empty list for non-existent news")
  void findByNewsId_returnsEmptyForNonExistentNews() {
    // Given
    Long nonExistentNewsId = 999L;

    // When
    List<Tag> tags = tagRepository.findByNewsId(nonExistentNewsId);

    // Then
    assertThat(tags).isEmpty();
  }

  @Test
  @DisplayName("count should return correct number of tags")
  void count_returnsCorrectNumber() {
    // When
    long count = tagRepository.count();

    // Then
    assertThat(count).isGreaterThanOrEqualTo(1);
  }
}
