package com.mjc.school.service.impl;

import com.mjc.school.repository.exception.EntityConflictRepositoryException;
import com.mjc.school.repository.impl.CommentRepository;
import com.mjc.school.repository.impl.NewsRepository;
import com.mjc.school.repository.impl.UserRepository;
import com.mjc.school.repository.model.Comment;
import com.mjc.school.repository.model.user.User;
import com.mjc.school.service.BaseService;
import com.mjc.school.service.dto.CommentsDtoRequest;
import com.mjc.school.service.dto.CommentsDtoResponse;
import com.mjc.school.service.dto.PageDtoResponse;
import com.mjc.school.service.dto.ResourceSearchFilterRequestDTO;
import com.mjc.school.service.exceptions.NotFoundException;
import com.mjc.school.service.exceptions.ResourceConflictServiceException;
import com.mjc.school.service.filter.ResourceSearchFilter;
import com.mjc.school.service.filter.mapper.CommentsSearchFilterMapper;
import com.mjc.school.service.mapper.CommentMapper;
import com.mjc.school.service.validator.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import static com.mjc.school.service.exceptions.ServiceErrorCode.COMMENT_CONFLICT;
import static com.mjc.school.service.exceptions.ServiceErrorCode.COMMENT_ID_DOES_NOT_EXIST;
import static com.mjc.school.service.exceptions.ServiceErrorCode.NEWS_ID_DOES_NOT_EXIST;
import static com.mjc.school.service.exceptions.ServiceErrorCode.USERNAME_DOES_NOT_EXIST;

@Service
public class CommentService
    implements BaseService<CommentsDtoRequest, CommentsDtoResponse, Long, ResourceSearchFilterRequestDTO, CommentsDtoRequest> {

    private final CommentRepository commentRepository;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final CommentMapper mapper;
    private final CommentsSearchFilterMapper commentsSearchFilterMapper;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          NewsRepository newsRepository,
                          UserRepository userRepository,
                          CommentMapper mapper,
                          CommentsSearchFilterMapper  commentsSearchFilterMapper) {
        this.commentRepository = commentRepository;
        this.newsRepository = newsRepository;
        this.userRepository=userRepository;
        this.mapper = mapper;
        this.commentsSearchFilterMapper = commentsSearchFilterMapper;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "comments",
            key = "'page-' + #searchFilterRequest.page + '-' + #searchFilterRequest.pageSize")
    public PageDtoResponse<CommentsDtoResponse> readAll(@Valid ResourceSearchFilterRequestDTO searchFilterRequest) {
        final ResourceSearchFilter searchFilter = commentsSearchFilterMapper.map(searchFilterRequest);
        final Specification<Comment> specification = getEntitySearchSpecification(searchFilter).getSearchFilterSpecification();
        final Pageable pageable = createPageable(searchFilter);
        final Page<Comment> page = commentRepository.findAll(specification,pageable);
        final List<CommentsDtoResponse> modelDtoList = mapper.modelListToDtoList(page.getContent());
        return new PageDtoResponse<>(modelDtoList, page.getNumber()+1, page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "#id")
    public CommentsDtoResponse readById(Long id) {
        return commentRepository.findById(id)
                .map(mapper::modelToDto)
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format(
                                        COMMENT_ID_DOES_NOT_EXIST.getMessage(),
                                        id
                                )
                        )
                );
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "comments", allEntries = true),
            @CacheEvict(value = "news",     allEntries = true)
    })
    public CommentsDtoResponse create(@Valid CommentsDtoRequest createRequest) {
        if (!newsRepository.existsById(createRequest.newsId())) {
            throw new NotFoundException(String.format(NEWS_ID_DOES_NOT_EXIST.getMessage(), createRequest.newsId()));
        }
        try {
            Comment model = mapper.dtoToModel(createRequest);
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new NotFoundException(String.format(USERNAME_DOES_NOT_EXIST.getMessage())));

            model.setUser(currentUser);

            model = commentRepository.save(model);
            return mapper.modelToDto(model);
        } catch (EntityConflictRepositoryException exc) {
            throw new ResourceConflictServiceException(COMMENT_CONFLICT.getMessage(), COMMENT_CONFLICT.getErrorCode(), exc.getMessage());
        }
    }

    @Override
    @Transactional
    @Caching(
            put  = { @CachePut(value = "comments", key = "#id") },
            evict = {
                    @CacheEvict(value = "comments", allEntries = true),
                    @CacheEvict(value = "news",     allEntries = true)
            }
    )
    public CommentsDtoResponse update(Long id, @Valid CommentsDtoRequest updateRequest) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(()->new NotFoundException(String.format(COMMENT_ID_DOES_NOT_EXIST.getMessage(), id)));

        if(updateRequest.content()!=null && !updateRequest.content().isBlank()){
            comment.setContent(updateRequest.content());
        }
        Comment updatedComment = commentRepository.save(comment);
        return mapper.modelToDto(updatedComment);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "comments", allEntries = true),
            @CacheEvict(value = "news", allEntries = true)
    })
    public void deleteById(Long id) {
        if (commentRepository.existsById(id)) {
            commentRepository.deleteById(id);
        } else {
            throw new NotFoundException(String.format(COMMENT_ID_DOES_NOT_EXIST.getMessage(), id));
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "'byNews-' + #newsId")
    public List<CommentsDtoResponse> readByNewsId(Long newsId) {
        return mapper.modelListToDtoList(commentRepository.findByNewsId(newsId));
    }
}
