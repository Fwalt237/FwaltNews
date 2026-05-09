package com.mjc.school.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
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
import com.mjc.school.service.config.CacheConfig;
import com.mjc.school.service.dto.AuthorDtoResponse;
import com.mjc.school.service.dto.CommentsDtoForNewsResponse;
import com.mjc.school.service.dto.CommentsDtoRequest;
import com.mjc.school.service.dto.CommentsDtoResponse;
import com.mjc.school.service.dto.NewsDtoResponse;
import com.mjc.school.service.dto.PageDtoResponse;
import com.mjc.school.service.dto.TagDtoRequest;
import com.mjc.school.service.dto.TagDtoResponse;
import com.mjc.school.service.dto.UpdateNewsDtoRequest;
import com.mjc.school.service.filter.mapper.AuthorSearchFilterMapper;
import com.mjc.school.service.filter.mapper.CommentsSearchFilterMapper;
import com.mjc.school.service.filter.mapper.NewsSearchFilterMapper;
import com.mjc.school.service.filter.mapper.TagSearchFilterMapper;
import com.mjc.school.service.mapper.AuthorMapper;
import com.mjc.school.service.mapper.CommentMapper;
import com.mjc.school.service.mapper.NewsMapper;
import com.mjc.school.service.mapper.TagMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = {CacheConfig.class, NewsService.class, AuthorService.class, TagService.class, CommentService.class})
@DisplayName("Service Layer Caching Integration Tests")
@ActiveProfiles("test")
@WithMockUser(username = "Commentator", roles = {"USER"})
class CaffeineServiceCacheTest {

    private static final String CACHE_NEWS = "news";
    private static final String CACHE_NEWS_PAGE = "newsPage";
    private static final String CACHE_AUTHORS = "authors";
    private static final String CACHE_TAGS = "tags";
    private static final String CACHE_COMMENTS = "comments";

    @MockBean private NewsRepository newsRepository;
    @MockBean private AuthorRepository authorRepository;
    @MockBean private TagRepository tagRepository;
    @MockBean private CommentRepository commentRepository;
    @MockBean private UserRepository userRepository;

    @MockBean private NewsMapper newsMapper;
    @MockBean private AuthorMapper authorMapper;
    @MockBean private TagMapper tagMapper;
    @MockBean private CommentMapper commentMapper;

    @MockBean private NewsSearchFilterMapper newsSearchFilterMapper;
    @MockBean private AuthorSearchFilterMapper authorSearchFilterMapper;
    @MockBean private TagSearchFilterMapper tagSearchFilterMapper;
    @MockBean private CommentsSearchFilterMapper commentSearchFilterMapper;

    @Autowired private NewsService newsService;
    @Autowired private AuthorService authorService;
    @Autowired private TagService tagService;
    @Autowired private CommentService commentService;
    @Autowired private CacheManager cacheManager;

    private News news;
    private Tag tag;
    private Comment comment;
    private Author author;
    private User user;
    private NewsDtoResponse newsDtoResponse;
    private TagDtoResponse tagDtoResponse;
    private CommentsDtoResponse commentDtoResponse;
    private CommentsDtoForNewsResponse commentsDtoForNewsResponse;
    private AuthorDtoResponse authorDtoResponse;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(name -> {
            Objects.requireNonNull(cacheManager.getCache(name)).clear();
        });

        initTestData();

        setupMockBehaviors();
    }

    @Test
    @DisplayName("Should hits DB only once, subsequent calls use cache")
    void newsReadById_secondCallFromCache() {
        newsService.readById(1L);
        newsService.readById(1L);

        verify(newsRepository, times(1)).findById(1L);
        assertThat(getNativeCache(CACHE_NEWS).getIfPresent(1L)).isNotNull();
    }

    @Test
    @DisplayName("Should evicts both 'news' and 'newsPage' caches")
    void newsDeleteById_evictsBothCaches() {
        cacheManager.getCache(CACHE_NEWS).put(1L, newsDtoResponse);
        cacheManager.getCache(CACHE_NEWS_PAGE).put("1-10-[]", new PageDtoResponse<>());
        when(newsRepository.existsById(1L)).thenReturn(true);

        newsService.deleteById(1L);

        assertThat(getNativeCache(CACHE_NEWS).getIfPresent(1L)).isNull();
        assertThat(getNativeCache(CACHE_NEWS_PAGE).estimatedSize()).isZero();
    }

    @Test
    @DisplayName("Should updates 'news' cache, clears 'newsPage' cache")
    void newsUpdate_putAndEvict() {

        cacheManager.getCache(CACHE_NEWS_PAGE).put("1-10-[]", new PageDtoResponse<>());
        when(newsRepository.save(any(News.class))).thenReturn(news);
        UpdateNewsDtoRequest req = new UpdateNewsDtoRequest("Updated Title", null, null, null, null, null, null);

        newsService.update(1L, req);

        assertThat(getNativeCache(CACHE_NEWS_PAGE).estimatedSize()).isZero();
        assertThat(getNativeCache(CACHE_NEWS).getIfPresent(1L)).isNotNull();
    }

    @Test
    @DisplayName("Read should caches it and Delete should evicts it")
    void authorCache_readAndEvict() {
        authorService.readById(1L);
        authorService.readById(1L);
        verify(authorRepository, times(1)).findById(1L);
        assertThat(getNativeCache(CACHE_AUTHORS).getIfPresent(1L)).isNotNull();

        when(authorRepository.existsById(1L)).thenReturn(true);
        authorService.deleteById(1L);
        assertThat(getNativeCache(CACHE_AUTHORS).getIfPresent(1L)).isNull();
    }

    @Test
    @DisplayName("Should Evicts all tag caches")
    void tagCreate_evictsAllTagEntries() {
        cacheManager.getCache(CACHE_TAGS).put(1L, tagDtoResponse);
        when(tagRepository.save(any(Tag.class))).thenReturn(tag);
        when(tagMapper.dtoToModel(any())).thenReturn(tag);

        tagService.create(new TagDtoRequest("new-tag"));

        assertThat(getNativeCache(CACHE_TAGS).estimatedSize()).isZero();
    }

    @Test
    @DisplayName("Should Evicts 'comments' and related 'news' caches")
    void commentCreate_evictsBothCaches() {
        cacheManager.getCache(CACHE_COMMENTS).put(1L, commentDtoResponse);
        cacheManager.getCache(CACHE_NEWS).put(1L, newsDtoResponse);

        when(newsRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.dtoToModel(any())).thenReturn(comment);

        commentService.create(new CommentsDtoRequest("A new comment", 1L));

        assertThat(getNativeCache(CACHE_COMMENTS).estimatedSize()).isZero();
        assertThat(getNativeCache(CACHE_NEWS).estimatedSize()).isZero();
    }

    private Cache<Object, Object> getNativeCache(String name) {
        org.springframework.cache.Cache springCache = cacheManager.getCache(name);
        assertThat(springCache).isNotNull();

        CaffeineCache caffeineCache = (CaffeineCache) springCache;
        assert caffeineCache != null;
        Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();

        nativeCache.cleanUp();
        return nativeCache;
    }

    private void initTestData() {
        LocalDateTime now = LocalDateTime.now();

        this.author = new Author();
        author.setId(1L);
        author.setName("James");

        this.tag = new Tag();
        tag.setId(1L);
        tag.setName("Technology");

        this.user = new User();
        user.setId(1L);
        user.setUsername("Commentator");

        this.comment = new Comment();
        comment.setId(1L);
        comment.setContent("Spring Boot");
        comment.setUser(user);

        this.news = new News();
        news.setId(1L);
        news.setTitle("Java");
        news.setAuthor(author);
        news.setTags(List.of(tag));

        this.authorDtoResponse = new AuthorDtoResponse(1L, "James", now, now);
        this.tagDtoResponse = new TagDtoResponse(1L, "Technology");
        this.commentDtoResponse = new CommentsDtoResponse(1L, "Spring Boot", 1L, "Commentator", now, now);
        this.commentsDtoForNewsResponse = new CommentsDtoForNewsResponse(1L, "Spring Boot", now, now);
        this.newsDtoResponse = new NewsDtoResponse(1L, "Java", "Language", "https://example.com/image.jpg", "https://example.com/icon.png", now, now,
                authorDtoResponse, List.of(tagDtoResponse), List.of(commentsDtoForNewsResponse));
    }

    @SuppressWarnings("unchecked")
    private void setupMockBehaviors() {

        when(newsMapper.modelToDto(any())).thenReturn(newsDtoResponse);
        when(authorMapper.modelToDto(any())).thenReturn(authorDtoResponse);
        when(tagMapper.modelToDto(any())).thenReturn(tagDtoResponse);
        when(commentMapper.modelToDto(any())).thenReturn(commentDtoResponse);

        when(newsRepository.findById(anyLong())).thenReturn(Optional.of(news));
        when(authorRepository.findById(anyLong())).thenReturn(Optional.of(author));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(tag));
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        when(userRepository.findByUsername("Commentator")).thenReturn(Optional.of(user));

        when(newsRepository.existsById(anyLong())).thenReturn(true);
        when(authorRepository.existsById(anyLong())).thenReturn(true);
        when(tagRepository.existsById(anyLong())).thenReturn(true);
        when(commentRepository.existsById(anyLong())).thenReturn(true);

        when(newsRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(news)));
        when(authorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(author)));
        when(tagRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(tag)));
        when(commentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(comment)));
    }
}



