package com.mjc.school.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.mjc.school.repository.impl.AuthorRepository;
import com.mjc.school.repository.impl.CommentRepository;
import com.mjc.school.repository.impl.NewsRepository;
import com.mjc.school.repository.impl.TagRepository;
import com.mjc.school.repository.impl.UserRepository;
import com.mjc.school.repository.model.Author;
import com.mjc.school.repository.model.Comment;
import com.mjc.school.repository.model.News;
import com.mjc.school.repository.model.Tag;
import com.mjc.school.repository.model.user.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DisplayName("CommentRepository integration tests")
class CommentRepositoryTest extends BaseRepositoryTest {

  @Autowired private CommentRepository commentRepository;
  @Autowired private NewsRepository newsRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private AuthorRepository authorRepository;
  @Autowired private TagRepository tagRepository;

  private User savedUser;
  private News savedNews;
  private Comment savedComment;

  @BeforeEach
  void setUp() {
    commentRepository.deleteAll();
    newsRepository.deleteAll();
    tagRepository.deleteAll();
    authorRepository.deleteAll();
    userRepository.deleteAll();

    User user = new User();
    user.setUsername("commenter");
    user.setEmail("commenter@example.com");
    user.setPassword("password");
    savedUser = userRepository.save(user);

    Author author = new Author();
    author.setName("Author For Comments");
    authorRepository.save(author);

    Tag tag = new Tag();
    tag.setName("discussion");
    tagRepository.save(tag);

    News news = new News();
    news.setTitle("Article with comments");
    news.setContent("Content that will receive comments...");
    news.setAuthor(author);
    news.setTags(List.of(tag));
    savedNews = newsRepository.save(news);

    Comment comment = new Comment();
    comment.setContent("This is a test comment");
    comment.setNews(savedNews);
    comment.setUser(savedUser);
    savedComment = commentRepository.save(comment);
  }

  @Test
  @DisplayName("save should persist comment with user, news and createdDate")
  void save_persistsCommentWithAssociationsAndAuditing() {
    // Given
    Comment newComment = new Comment();
    newComment.setContent("Fresh comment");
    newComment.setNews(savedNews);
    newComment.setUser(savedUser);

    // When
    Comment saved = commentRepository.save(newComment);

    // Then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getContent()).isEqualTo("Fresh comment");
    assertThat(saved.getCreatedDate()).isNotNull();
    assertThat(saved.getUser().getUsername()).isEqualTo("commenter");
    assertThat(saved.getNews().getTitle()).isEqualTo("Article with comments");
  }

  @Test
  @DisplayName("findById should return comment when it exists")
  void findById_returnsCommentWhenExists() {
    // Given
    Long existingId = savedComment.getId();

    // When
    Optional<Comment> found = commentRepository.findById(existingId);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getContent()).isEqualTo("This is a test comment");
  }

  @Test
  @DisplayName("findById should return empty when comment does not exist")
  void findById_returnsEmptyWhenNotExists() {
    // Given
    Long nonExistentId = 999L;

    // When
    Optional<Comment> found = commentRepository.findById(nonExistentId);

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("findAll with pagination should return correct page")
  void findAll_withPagination_returnsCorrectPage() {
    // Given
    Comment second = new Comment();
    second.setContent("Second comment");
    second.setNews(savedNews);
    second.setUser(savedUser);
    commentRepository.save(second);

    // When
    Page<Comment> page =
        commentRepository.findAll(PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "content")));

    // Then
    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
    assertThat(page.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("update should modify content and update lastUpdatedDate")
  void update_modifiesContentAndUpdatesLastUpdatedDate() {
    // Given
    savedComment.setContent("Updated comment text");

    // When
    Comment updated = commentRepository.save(savedComment);

    // Then
    assertThat(updated.getContent()).isEqualTo("Updated comment text");
    assertThat(updated.getLastUpdatedDate()).isNotNull();
  }

  @Test
  @DisplayName("deleteById should remove comment from database")
  void deleteById_removesComment() {
    // Given
    Long existingId = savedComment.getId();

    // When
    commentRepository.deleteById(existingId);

    // Then
    assertThat(commentRepository.findById(existingId)).isEmpty();
  }

  @Test
  @DisplayName("existsById should return true for existing comment")
  void existsById_returnsTrueForExistingComment() {
    // Given
    Long existingId = savedComment.getId();

    // When
    boolean exists = commentRepository.existsById(existingId);

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("existsById should return false for non-existent comment")
  void existsById_returnsFalseForNonExistentComment() {
    // Given
    Long nonExistentId = 999L;

    // When
    boolean exists = commentRepository.existsById(nonExistentId);

    // Then
    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("findByNewsId should return comments for a given news article")
  void findByNewsId_returnsCommentsForNews() {
    // Given
    Comment another = new Comment();
    another.setContent("Another comment");
    another.setNews(savedNews);
    another.setUser(savedUser);
    commentRepository.save(another);

    // When
    List<Comment> comments = commentRepository.findByNewsId(savedNews.getId());

    // Then
    assertThat(comments).hasSize(2);
    assertThat(comments)
        .extracting(Comment::getContent)
        .containsExactlyInAnyOrder("This is a test comment", "Another comment");
  }

  @Test
  @DisplayName("findByNewsId should return empty list when news has no comments")
  void findByNewsId_returnsEmptyForNewsWithoutComments() {
    // Given
    Author author = new Author();
    author.setName("NoCommentAuthor");
    authorRepository.save(author);

    Tag tag = new Tag();
    tag.setName("empty");
    tagRepository.save(tag);

    News lonelyNews = new News();
    lonelyNews.setTitle("News with no comments");
    lonelyNews.setContent("No one is commenting...");
    lonelyNews.setAuthor(author);
    lonelyNews.setTags(List.of(tag));
    newsRepository.save(lonelyNews);

    // When
    List<Comment> comments = commentRepository.findByNewsId(lonelyNews.getId());

    // Then
    assertThat(comments).isEmpty();
  }

  @Test
  @DisplayName("findByNewsId should return empty list for non-existent news id")
  void findByNewsId_returnsEmptyForNonExistentNewsId() {
    // Given
    Long nonExistentNewsId = 999L;

    // When
    List<Comment> comments = commentRepository.findByNewsId(nonExistentNewsId);

    // Then
    assertThat(comments).isEmpty();
  }
}
