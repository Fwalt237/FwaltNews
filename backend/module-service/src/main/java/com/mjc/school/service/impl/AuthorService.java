package com.mjc.school.service.impl;

import com.mjc.school.repository.exception.EntityConflictRepositoryException;
import com.mjc.school.repository.impl.AuthorRepository;
import com.mjc.school.repository.model.Author;
import com.mjc.school.service.BaseService;
import com.mjc.school.service.dto.AuthorDtoRequest;
import com.mjc.school.service.dto.AuthorDtoResponse;
import com.mjc.school.service.dto.PageDtoResponse;
import com.mjc.school.service.dto.ResourceSearchFilterRequestDTO;
import com.mjc.school.service.exceptions.NotFoundException;
import com.mjc.school.service.exceptions.ResourceConflictServiceException;
import com.mjc.school.service.filter.ResourceSearchFilter;
import com.mjc.school.service.filter.mapper.AuthorSearchFilterMapper;
import com.mjc.school.service.mapper.AuthorMapper;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import static com.mjc.school.service.exceptions.ServiceErrorCode.AUTHOR_CONFLICT;
import static com.mjc.school.service.exceptions.ServiceErrorCode.AUTHOR_DOES_NOT_EXIST_FOR_NEWS_ID;
import static com.mjc.school.service.exceptions.ServiceErrorCode.AUTHOR_ID_DOES_NOT_EXIST;

@Service
public class AuthorService
    implements BaseService<AuthorDtoRequest, AuthorDtoResponse, Long, ResourceSearchFilterRequestDTO, AuthorDtoRequest> {

    private final AuthorRepository authorRepository;
    private final AuthorMapper mapper;
    private final AuthorSearchFilterMapper authorSearchFilterMapper;

    @Autowired
    public AuthorService(AuthorRepository authorRepository, AuthorMapper mapper, AuthorSearchFilterMapper authorSearchFilterMapper) {
        this.authorRepository = authorRepository;
        this.mapper = mapper;
        this.authorSearchFilterMapper = authorSearchFilterMapper;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "authors",
            key = "'page-' + #searchFilterRequest.page + '-' + #searchFilterRequest.pageSize")
    public PageDtoResponse<AuthorDtoResponse> readAll(@Valid ResourceSearchFilterRequestDTO searchFilterRequest) {
        final ResourceSearchFilter searchFilter = authorSearchFilterMapper.map(searchFilterRequest);
        final Specification<Author> specification = getEntitySearchSpecification(searchFilter).getSearchFilterSpecification();
        final Pageable pageable = createPageable(searchFilter);
        final Page<Author> page = authorRepository.findAll(specification,pageable);
        final List<AuthorDtoResponse> modelDtoList = mapper.modelListToDtoList(page.getContent());
        return new PageDtoResponse<>(modelDtoList, page.getNumber()+1, page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "authors", key = "#id")
    public AuthorDtoResponse readById(Long id) {
        return authorRepository.findById(id)
            .map(mapper::modelToDto)
            .orElseThrow(
                () -> new NotFoundException(
                    String.format(
                        AUTHOR_ID_DOES_NOT_EXIST.getMessage(),
                        id
                    )
                )
            );
    }

    @Override
    @Transactional
    @CacheEvict(value = "authors", allEntries = true)
    public AuthorDtoResponse create(@Valid AuthorDtoRequest createRequest) {
        try {
            Author model = mapper.dtoToModel(createRequest);
            model = authorRepository.save(model);
            return mapper.modelToDto(model);
        } catch (EntityConflictRepositoryException exc) {
            throw new ResourceConflictServiceException(AUTHOR_CONFLICT.getMessage(), AUTHOR_CONFLICT.getErrorCode(), exc.getMessage());
        }
    }

    @Override
    @Transactional
    @Caching(
            put  = { @CachePut(value = "authors", key = "#id") },
            evict = { @CacheEvict(value = "authors", allEntries = true) }
    )
    public AuthorDtoResponse update(Long id, @Valid AuthorDtoRequest updateRequest) {
        Author author = authorRepository.findById(id)
                .orElseThrow(()->new NotFoundException(String.format(AUTHOR_ID_DOES_NOT_EXIST.getMessage(), id)));

        if(updateRequest.name()!=null && !updateRequest.name().isBlank()){
            author.setName(updateRequest.name());
        }
        Author updatedAuthor = authorRepository.save(author);
        return mapper.modelToDto(updatedAuthor);

    }

    @Override
    @Transactional
    @CacheEvict(value = "authors", allEntries = true)
    public void deleteById(Long id) {
        if (authorRepository.existsById(id)) {
            authorRepository.deleteById(id);
        } else {
            throw new NotFoundException(String.format(AUTHOR_ID_DOES_NOT_EXIST.getMessage(), id));
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "authors", key = "'byNews-' + #newsId")
    public AuthorDtoResponse readByNewsId(Long newsId) {
        return authorRepository.findByNewsId(newsId)
            .map(mapper::modelToDto)
            .orElseThrow(
                () ->
                    new NotFoundException(String.format(AUTHOR_DOES_NOT_EXIST_FOR_NEWS_ID.getMessage(), newsId))
            );
    }
}
