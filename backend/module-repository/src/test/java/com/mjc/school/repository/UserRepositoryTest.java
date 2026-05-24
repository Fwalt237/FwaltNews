package com.mjc.school.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.mjc.school.repository.impl.AuthorRepository;
import com.mjc.school.repository.impl.CommentRepository;
import com.mjc.school.repository.impl.NewsRepository;
import com.mjc.school.repository.impl.TagRepository;
import com.mjc.school.repository.impl.UserRepository;
import com.mjc.school.repository.model.user.Role;
import com.mjc.school.repository.model.user.User;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DisplayName("UserRepository integration tests")
class UserRepositoryTest extends BaseRepositoryTest {

  @Autowired private UserRepository userRepository;
  @Autowired private CommentRepository commentRepository;
  @Autowired private NewsRepository newsRepository;
  @Autowired private TagRepository tagRepository;
  @Autowired private AuthorRepository authorRepository;

  private User savedUser;

  @BeforeEach
  void setUp() {
    commentRepository.deleteAll();
    newsRepository.deleteAll();
    tagRepository.deleteAll();
    authorRepository.deleteAll();
    userRepository.deleteAll();

    User user = new User();
    user.setUsername("testuser");
    user.setEmail("testuser@example.com");
    user.setPassword("secret");
    user.setFirstName("Test");
    user.setLastName("User");
    user.setRoles(new HashSet<>(Set.of(Role.ROLE_USER)));
    savedUser = userRepository.save(user);
  }

  @Test
  @DisplayName("save should persist user and generate id")
  void save_persistsUserAndGeneratesId() {
    // Given
    User newUser = new User();
    newUser.setUsername("newbie");
    newUser.setEmail("newbie@example.com");
    newUser.setPassword("pass");
    newUser.setRoles(Set.of(Role.ROLE_USER));

    // When
    User saved = userRepository.save(newUser);

    // Then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getUsername()).isEqualTo("newbie");
    assertThat(saved.getEmail()).isEqualTo("newbie@example.com");
    assertThat(saved.getCreatedDate()).isNotNull();
  }

  @Test
  @DisplayName("findById should return user when it exists")
  void findById_returnsUserWhenExists() {
    // Given
    Long existingId = savedUser.getId();

    // When
    Optional<User> found = userRepository.findById(existingId);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getUsername()).isEqualTo("testuser");
  }

  @Test
  @DisplayName("findById should return empty when user does not exist")
  void findById_returnsEmptyWhenNotExists() {
    // Given
    Long nonExistentId = 999L;

    // When
    Optional<User> found = userRepository.findById(nonExistentId);

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("findAll with pagination should return correct page")
  void findAll_withPagination_returnsCorrectPage() {
    // Given
    User second = new User();
    second.setUsername("another");
    second.setEmail("another@example.com");
    second.setPassword("pass");
    userRepository.save(second);

    // When
    Page<User> page =
        userRepository.findAll(PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "username")));

    // Then
    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
    assertThat(page.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("update should modify username and update lastModifiedDate")
  void update_modifiesUsernameAndUpdatesLastModifiedDate() {
    // Given
    savedUser.setUsername("updateduser");

    // When
    User updated = userRepository.save(savedUser);

    // Then
    assertThat(updated.getUsername()).isEqualTo("updateduser");
    assertThat(updated.getLastModifiedDate()).isNotNull();
  }

  @Test
  @DisplayName("deleteById should remove user from database")
  void deleteById_removesUser() {
    // Given
    Long existingId = savedUser.getId();

    // When
    userRepository.deleteById(existingId);

    // Then
    assertThat(userRepository.findById(existingId)).isEmpty();
  }

  @Test
  @DisplayName("existsById should return true for existing user")
  void existsById_returnsTrueForExistingUser() {
    // Given
    Long existingId = savedUser.getId();

    // When
    boolean exists = userRepository.existsById(existingId);

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("existsById should return false for non-existent user")
  void existsById_returnsFalseForNonExistentUser() {
    // Given
    Long nonExistentId = 999L;

    // When
    boolean exists = userRepository.existsById(nonExistentId);

    // Then
    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("findByUsername should return user matching exact username")
  void findByUsername_returnsMatchingUser() {
    // Given
    String username = "testuser";

    // When
    Optional<User> found = userRepository.findByUsername(username);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(savedUser.getId());
  }

  @Test
  @DisplayName("findByUsername should return empty for non-existent username")
  void findByUsername_returnsEmptyForNonExistentUsername() {
    // Given
    String unknownUsername = "ghost";

    // When
    Optional<User> found = userRepository.findByUsername(unknownUsername);

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("findByEmail should return user matching email")
  void findByEmail_returnsMatchingUser() {
    // Given
    String email = "testuser@example.com";

    // When
    Optional<User> found = userRepository.findByEmail(email);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getUsername()).isEqualTo("testuser");
  }

  @Test
  @DisplayName("findByEmail should return empty for unknown email")
  void findByEmail_returnsEmptyForUnknownEmail() {
    // Given
    String unknownEmail = "unknown@example.com";

    // When
    Optional<User> found = userRepository.findByEmail(unknownEmail);

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("existsByUsername should return true for existing username")
  void existsByUsername_returnsTrueWhenExists() {
    // Given
    String username = "testuser";

    // When
    boolean exists = userRepository.existsByUsername(username);

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("existsByUsername should return false for non-existent username")
  void existsByUsername_returnsFalseWhenNotExists() {
    // Given
    String username = "nobody";

    // When
    boolean exists = userRepository.existsByUsername(username);

    // Then
    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("existsByEmail should return true for existing email")
  void existsByEmail_returnsTrueWhenExists() {
    // Given
    String email = "testuser@example.com";

    // When
    boolean exists = userRepository.existsByEmail(email);

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("existsByEmail should return false for non-existent email")
  void existsByEmail_returnsFalseWhenNotExists() {
    // Given
    String email = "missing@example.com";

    // When
    boolean exists = userRepository.existsByEmail(email);

    // Then
    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("count should return correct number of users")
  void count_returnsCorrectNumber() {
    // When
    long count = userRepository.count();

    // Then
    assertThat(count).isGreaterThanOrEqualTo(1);
  }
}
