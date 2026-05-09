package com.mjc.school.service.impl;


import com.mjc.school.repository.filter.pagination.Pagination;
import com.mjc.school.repository.impl.AuthorRepository;
import com.mjc.school.repository.model.Author;
import com.mjc.school.service.dto.AuthorDtoRequest;
import com.mjc.school.service.dto.AuthorDtoResponse;
import com.mjc.school.service.dto.PageDtoResponse;
import com.mjc.school.service.dto.ResourceSearchFilterRequestDTO;
import com.mjc.school.service.exceptions.NotFoundException;
import com.mjc.school.service.filter.ResourceSearchFilter;
import com.mjc.school.service.filter.mapper.AuthorSearchFilterMapper;
import com.mjc.school.service.mapper.AuthorMapper;
import java.time.LocalDateTime;
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
@DisplayName("Author Service Unit tests")
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private AuthorMapper mapper;

    @Mock
    private AuthorSearchFilterMapper authorSearchFilterMapper;

    @InjectMocks
    private AuthorService authorService;

    private Author author;
    private AuthorDtoRequest authorDtoRequest;
    private AuthorDtoResponse authorDtoResponse;

    @BeforeEach
    void setUp(){
        author = new Author();
        author.setId(1L);
        author.setName("James");
        author.setCreatedDate(LocalDateTime.now());
        author.setLastUpdatedDate(LocalDateTime.now());

        authorDtoRequest = new AuthorDtoRequest("James");
        authorDtoResponse = new AuthorDtoResponse(1L,"James",LocalDateTime.now(),LocalDateTime.now());
    }


    @Test
    @DisplayName("Should return all authors with pagination")
    void readAll_ShouldReturnPagedAuthors(){

        ResourceSearchFilterRequestDTO searchRequest = new ResourceSearchFilterRequestDTO(1,10, Collections.emptyList(),Collections.emptyList());
        Page<Author> page = new PageImpl<>(List.of(author));

        when(authorSearchFilterMapper.map(any())).thenReturn(new ResourceSearchFilter(new Pagination(1, 10),Collections.emptyList(),Collections.emptyList()));
        when(authorRepository.findAll(ArgumentMatchers.<Specification<Author>>any(),any(Pageable.class))).thenReturn(page);
        when(mapper.modelListToDtoList(anyList())).thenReturn(List.of(authorDtoResponse));

        PageDtoResponse<AuthorDtoResponse> result = authorService.readAll(searchRequest);

        assertThat(result).isNotNull();
        assertThat(result.getModelDtoList()).hasSize(1);
        assertThat(result.getCurrentPage()).isEqualTo(1);
        assertThat(result.getPageCount()).isEqualTo(1);
        verify(authorRepository).findAll(ArgumentMatchers.<Specification<Author>>any(),any(Pageable.class));
    }

    @Test
    @DisplayName("Should return author ID when author exists")
    void readById_WhenAuthorExists_ShouldReturnAuthor(){

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(mapper.modelToDto(author)).thenReturn(authorDtoResponse);

        AuthorDtoResponse result = authorService.readById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("James");
        verify(authorRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when author does not exist")
    void readById_WhenAuthorDoesNotExist_ShouldThrowNotFoundException(){

        when(authorRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(()->authorService.readById(2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Author with id 2 does not exist.");

        verify(authorRepository).findById(2L);
    }

    @Test
    @DisplayName("Should create author successfully")
    void create_ShouldCreateAndReturnAuthor(){

        when(mapper.dtoToModel(authorDtoRequest)).thenReturn(author);
        when(authorRepository.save(author)).thenReturn(author);
        when(mapper.modelToDto(author)).thenReturn(authorDtoResponse);

        AuthorDtoResponse result = authorService.create(authorDtoRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("James");
        verify(authorRepository).save(author);
    }

    @Test
    @DisplayName("Should update author when author exists")
    void update_WhenAuthorExists_ShouldUpdateAuthor(){

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorRepository.save(author)).thenReturn(author);
        when(mapper.modelToDto(author)).thenReturn(authorDtoResponse);

        AuthorDtoResponse result = authorService.update(1L,authorDtoRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(authorRepository).save(author);
    }

    @Test
    @DisplayName("Should throw NotFoundException when updating non-existent author")
    void update_WhenAuthorDoesNotExist_ShouldThrowNotFoundException(){

        when(authorRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(()->authorService.update(2L,authorDtoRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Author with id 2 does not exist.");

        verify(authorRepository,never()).save(any());
    }

    @Test
    @DisplayName("Should delete author when author exists")
    void deleteById_WhenAuthorExists_ShouldDeleteAuthor(){

        when(authorRepository.existsById(1L)).thenReturn(true);
        doNothing().when(authorRepository).deleteById(1L);

        authorService.deleteById(1L);

        verify(authorRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent author")
    void deleteById_WhenAuthorDoesNotExist_ShouldThrowNotFoundException(){

        when(authorRepository.existsById(2L)).thenReturn(false);

        assertThatThrownBy(()->authorService.deleteById(2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Author with id 2 does not exist.");

        verify(authorRepository,never()).deleteById(any());
    }

    @Test
    @DisplayName("Should return author by news ID")
    void readByNewsId_WhenAuthorExists_ShouldReturnAuthor(){

        when(authorRepository.findByNewsId(1L)).thenReturn(Optional.of(author));
        when(mapper.modelToDto(author)).thenReturn(authorDtoResponse);

        AuthorDtoResponse result = authorService.readByNewsId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("James");
        verify(authorRepository).findByNewsId(1L);
    }
}
