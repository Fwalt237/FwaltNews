package com.mjc.school.service.impl;

import com.mjc.school.repository.filter.pagination.Pagination;
import com.mjc.school.repository.impl.CommentRepository;
import com.mjc.school.repository.impl.NewsRepository;
import com.mjc.school.repository.impl.UserRepository;
import com.mjc.school.repository.model.Comment;
import com.mjc.school.repository.model.user.User;
import com.mjc.school.service.dto.CommentsDtoRequest;
import com.mjc.school.service.dto.CommentsDtoResponse;
import com.mjc.school.service.dto.PageDtoResponse;
import com.mjc.school.service.dto.ResourceSearchFilterRequestDTO;
import com.mjc.school.service.exceptions.NotFoundException;
import com.mjc.school.service.filter.ResourceSearchFilter;
import com.mjc.school.service.filter.mapper.CommentsSearchFilterMapper;
import com.mjc.school.service.mapper.CommentMapper;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Comment Service Unit tests")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private CommentMapper mapper;

    @Mock
    private CommentsSearchFilterMapper commentSearchFilterMapper;

    @InjectMocks
    private CommentService commentService;

    private Comment comment;
    private CommentsDtoRequest commentDtoRequest;
    private CommentsDtoResponse commentDtoResponse;
    private User user;

    @BeforeEach
    void setUp(){
        user = new User();
        user.setId(1L);
        user.setUsername("Commentator");

        comment = new Comment();
        comment.setId(1L);
        comment.setContent("Spring Boot");
        comment.setUser(user);
        comment.setCreatedDate(LocalDateTime.now());
        comment.setLastUpdatedDate(LocalDateTime.now());

        commentDtoRequest = new CommentsDtoRequest("Spring Boot",1L);
        commentDtoResponse = new CommentsDtoResponse(1L,"Spring Boot",1L,"Commentator",LocalDateTime.now(),LocalDateTime.now());

    }

    private void mockUserAuthentication(String username) {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(auth.getName()).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should return all comments with pagination")
    void readAll_ShouldReturnPagedComments(){

        ResourceSearchFilterRequestDTO searchRequest = new ResourceSearchFilterRequestDTO(1,10, Collections.emptyList(),Collections.emptyList());
        Page<Comment> page = new PageImpl<>(List.of(comment));

        when(commentSearchFilterMapper.map(any())).thenReturn(new ResourceSearchFilter(new Pagination(1, 10),Collections.emptyList(),Collections.emptyList()));
        when(commentRepository.findAll(ArgumentMatchers.<Specification<Comment>>any(),any(Pageable.class))).thenReturn(page);
        when(mapper.modelListToDtoList(anyList())).thenReturn(List.of(commentDtoResponse));

        PageDtoResponse<CommentsDtoResponse> result = commentService.readAll(searchRequest);

        assertThat(result).isNotNull();
        assertThat(result.getModelDtoList()).hasSize(1);
        assertThat(result.getCurrentPage()).isEqualTo(1);
        assertThat(result.getPageCount()).isEqualTo(1);
        verify(commentRepository).findAll(ArgumentMatchers.<Specification<Comment>>any(),any(Pageable.class));
    }

    @Test
    @DisplayName("Should return comment ID when tag exists")
    void readById_WhenCommentExists_ShouldReturnComment(){

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(mapper.modelToDto(comment)).thenReturn(commentDtoResponse);

        CommentsDtoResponse result = commentService.readById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("Spring Boot");
        verify(commentRepository).findById(1L);
    }


    @Test
    @DisplayName("Should throw NotFoundException when comment does not exist")
    void readById_WhenCommentDoesNotExist_ShouldThrowNotFoundException(){

        when(commentRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(()->commentService.readById(2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Comment with id 2 does not exist.");

        verify(commentRepository).findById(2L);
    }


    @Test
    @DisplayName("Should create comment successfully")
    void create_ShouldCreateAndReturnComment(){

        mockUserAuthentication("Commentator");

        when(newsRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findByUsername("Commentator")).thenReturn(Optional.of(user));
        when(mapper.dtoToModel(commentDtoRequest)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(mapper.modelToDto(comment)).thenReturn(commentDtoResponse);

        CommentsDtoResponse result = commentService.create(commentDtoRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("Spring Boot");
        verify(userRepository).findByUsername("Commentator");
        verify(commentRepository).save(comment);
    }


    @Test
    @DisplayName("Should update comment when tag exists")
    void update_WhenCommentExists_ShouldUpdateComment(){

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);
        when(mapper.modelToDto(comment)).thenReturn(commentDtoResponse);

        CommentsDtoResponse result = commentService.update(1L,commentDtoRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(commentRepository).save(comment);
    }


    @Test
    @DisplayName("Should throw NotFoundException when updating non-existent comment")
    void update_WhenCommentDoesNotExist_ShouldThrowNotFoundException(){

        when(commentRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(()->commentService.update(2L,commentDtoRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Comment with id 2 does not exist.");

        verify(commentRepository,never()).save(any());
    }


    @Test
    @DisplayName("Should delete comment when comment exists")
    void deleteById_WhenCommentExists_ShouldDeleteComment(){

        when(commentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(commentRepository).deleteById(1L);

        commentService.deleteById(1L);

        verify(commentRepository).deleteById(1L);
    }


    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent comment")
    void deleteById_WhenCommentDoesNotExist_ShouldThrowNotFoundException(){

        when(commentRepository.existsById(2L)).thenReturn(false);

        assertThatThrownBy(()->commentService.deleteById(2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Comment with id 2 does not exist.");

        verify(commentRepository,never()).deleteById(any());
    }


    @Test
    @DisplayName("Should return comments by news ID")
    void readByNewsId_WhenCommentExists_ShouldReturnComment(){

        when(commentRepository.findByNewsId(1L)).thenReturn(List.of(comment));
        when(mapper.modelListToDtoList(List.of(comment))).thenReturn(List.of(commentDtoResponse));

        List<CommentsDtoResponse> result = commentService.readByNewsId(1L);

        assertThat(result).isNotNull().hasSizeGreaterThan(0);
        verify(commentRepository).findByNewsId(1L);
    }
}
