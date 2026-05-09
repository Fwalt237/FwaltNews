package com.mjc.school.service.impl;

import com.mjc.school.repository.filter.pagination.Pagination;
import com.mjc.school.repository.impl.AuthorRepository;
import com.mjc.school.repository.impl.NewsRepository;
import com.mjc.school.repository.impl.TagRepository;
import com.mjc.school.repository.model.Author;
import com.mjc.school.repository.model.News;
import com.mjc.school.repository.model.Tag;
import com.mjc.school.service.dto.AuthorDtoResponse;
import com.mjc.school.service.dto.CreateNewsDtoRequest;
import com.mjc.school.service.dto.NewsDtoResponse;
import com.mjc.school.service.dto.PageDtoResponse;
import com.mjc.school.service.dto.ResourceSearchFilterRequestDTO;
import com.mjc.school.service.dto.TagDtoResponse;
import com.mjc.school.service.dto.UpdateNewsDtoRequest;
import com.mjc.school.service.exceptions.NotFoundException;
import com.mjc.school.service.filter.ResourceSearchFilter;
import com.mjc.school.service.filter.mapper.NewsSearchFilterMapper;
import com.mjc.school.service.mapper.NewsMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("News Service Unit tests")
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private NewsMapper mapper;

    @Mock
    private NewsSearchFilterMapper newsSearchFilterMapper;

    @InjectMocks
    private NewsService newsService;

    private News news;
    private CreateNewsDtoRequest createRequest;
    private UpdateNewsDtoRequest updateRequest;
    private NewsDtoResponse newsDtoResponse;

    @BeforeEach
    void setUp(){
        news = new News();
        news.setId(1L);
        news.setTitle("Java");
        news.setContent("Language");
        news.setCreatedDate(LocalDateTime.now());
        news.setLastUpdatedDate(LocalDateTime.now());
        news.setTags(new ArrayList<>());
        news.setComments(new ArrayList<>());

        createRequest = new CreateNewsDtoRequest("Java","Language","https://example.com/image.jpg","https://example.com/icon.png","Gosling", List.of("Technology"),new ArrayList<>());
        updateRequest = new UpdateNewsDtoRequest("Java","Language","https://example.com/image.jpg","https://example.com/icon.png","Gosling", List.of("Technology"),new ArrayList<>());
        newsDtoResponse  = new NewsDtoResponse(1L,"Java","Language","https://example.com/image.jpg","https://example.com/icon.png",LocalDateTime.now(),LocalDateTime.now(),
                new AuthorDtoResponse(1L,"Gosling",LocalDateTime.now(),LocalDateTime.now()),
                List.of(new TagDtoResponse(1L,"Technology")),
                new ArrayList<>());

    }

    @Test
    @DisplayName("Should return all news with pagination")
    void readAll_ShouldReturnPagedNews(){

        ResourceSearchFilterRequestDTO searchRequest = new ResourceSearchFilterRequestDTO(1,10, Collections.emptyList(),Collections.emptyList());
        Page<News> page = new PageImpl<>(List.of(news));

        when(newsSearchFilterMapper.map(any())).thenReturn(new ResourceSearchFilter(new Pagination(1, 10),Collections.emptyList(),Collections.emptyList()));
        when(newsRepository.findAll(ArgumentMatchers.<Specification<News>>any(),any(Pageable.class))).thenReturn(page);
        when(mapper.modelListToDtoList(anyList())).thenReturn(List.of(newsDtoResponse));

        PageDtoResponse<NewsDtoResponse> result = newsService.readAll(searchRequest);

        assertThat(result).isNotNull();
        assertThat(result.getModelDtoList()).hasSize(1);
        assertThat(result.getCurrentPage()).isEqualTo(1);
        assertThat(result.getPageCount()).isEqualTo(1);
        verify(newsRepository).findAll(ArgumentMatchers.<Specification<News>>any(),any(Pageable.class));
    }

    @Test
    @DisplayName("Should return news ID when news exists")
    void readById_WhenNewsExists_ShouldReturnNews(){

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(mapper.modelToDto(news)).thenReturn(newsDtoResponse);

        NewsDtoResponse result = newsService.readById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Java");
        assertThat(result.getContent()).isEqualTo("Language");

        verify(newsRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when news does not exist")
    void readById_WhenNewsDoesNotExist_ShouldThrowNotFoundException(){

        when(newsRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(()->newsService.readById(2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("News with id 2 does not exist.");

        verify(newsRepository).findById(2L);
    }

    @Test
    @DisplayName("Should create news successfully with new author and tags")
    void create_ShouldCreateNewsWithNewAuthorAndTags(){
        when(authorRepository.findByName("Gosling")).thenReturn(Optional.empty());
        when(tagRepository.findByName("Technology")).thenReturn(Optional.empty());

        when(mapper.dtoToModel(createRequest)).thenReturn(news);
        when(newsRepository.save(news)).thenReturn(news);
        when(mapper.modelToDto(news)).thenReturn(newsDtoResponse);

        NewsDtoResponse result = newsService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("Language");
        verify(authorRepository).save(any(Author.class));
        verify(tagRepository).save(any(Tag.class));
        verify(newsRepository).save(news);
    }

    @Test
    @DisplayName("Should update news with new author and tag when news exists")
    void update_WhenNewsExists_ShouldUpdateNews(){
        Author mockAuthor = new Author();
        mockAuthor.setId(1L);
        mockAuthor.setName("Gosling");

        Tag mockTag = new Tag();
        mockTag.setId(1L);
        mockTag.setName("Technology");

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));

        when(authorRepository.findByName("Gosling"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(mockAuthor));

        when(tagRepository.findByName("Technology"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(mockTag));

        when(newsRepository.save(any(News.class))).thenReturn(news);
        when(mapper.modelToDto(news)).thenReturn(newsDtoResponse);

        NewsDtoResponse result = newsService.update(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Language");
        verify(authorRepository).save(any(Author.class));
        verify(tagRepository).save(any(Tag.class));
        verify(newsRepository).save(any(News.class));
    }

    @Test
    @DisplayName("Should delete news when news exists")
    void deleteById_WhenNewsExists_ShouldDeleteNews(){

        when(newsRepository.existsById(1L)).thenReturn(true);
        doNothing().when(newsRepository).deleteById(1L);

        newsService.deleteById(1L);

        verify(newsRepository).deleteById(1L);
    }


    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent news")
    void deleteById_WhenCommentDoesNotExist_ShouldThrowNotFoundException(){

        when(newsRepository.existsById(2L)).thenReturn(false);

        assertThatThrownBy(()->newsService.deleteById(2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("News with id 2 does not exist.");

        verify(newsRepository,never()).deleteById(any());
    }
}
