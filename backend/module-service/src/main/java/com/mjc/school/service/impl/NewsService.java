package com.mjc.school.service.impl;

import com.mjc.school.repository.exception.EntityConflictRepositoryException;
import com.mjc.school.repository.impl.AuthorRepository;
import com.mjc.school.repository.impl.NewsRepository;
import com.mjc.school.repository.impl.TagRepository;
import com.mjc.school.repository.model.Author;
import com.mjc.school.repository.model.News;
import com.mjc.school.repository.model.Tag;
import com.mjc.school.service.BaseService;
import com.mjc.school.service.dto.CreateNewsDtoRequest;
import com.mjc.school.service.dto.NewsDtoResponse;
import com.mjc.school.service.dto.PageDtoResponse;
import com.mjc.school.service.dto.ResourceSearchFilterRequestDTO;
import com.mjc.school.service.dto.UpdateNewsDtoRequest;
import com.mjc.school.service.exceptions.NotFoundException;
import com.mjc.school.service.exceptions.ResourceConflictServiceException;
import com.mjc.school.service.filter.ResourceSearchFilter;
import com.mjc.school.service.filter.mapper.NewsSearchFilterMapper;
import com.mjc.school.service.mapper.NewsMapper;
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


import static com.mjc.school.service.exceptions.ServiceErrorCode.NEWS_CONFLICT;
import static com.mjc.school.service.exceptions.ServiceErrorCode.NEWS_ID_DOES_NOT_EXIST;

@Service
public class NewsService implements BaseService<CreateNewsDtoRequest, NewsDtoResponse, Long, ResourceSearchFilterRequestDTO, UpdateNewsDtoRequest> {

    private final NewsRepository newsRepository;
    private final AuthorRepository authorRepository;
    private final TagRepository tagRepository;
    private final NewsMapper mapper;
    private final NewsSearchFilterMapper newsSearchFilterMapper;

    @Autowired
    public NewsService(
            final NewsRepository newsRepository,
            final AuthorRepository authorRepository,
            final TagRepository tagRepository,
            final NewsMapper mapper,
            final NewsSearchFilterMapper newsSearchFilterMapper
    ) {
        this.newsRepository = newsRepository;
        this.authorRepository = authorRepository;
        this.tagRepository = tagRepository;
        this.mapper = mapper;
        this.newsSearchFilterMapper = newsSearchFilterMapper;
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value="newsPage",key="#searchFilterRequest.page + '-' + #searchFilterRequest.pageSize + '-' "
            + "+ #searchFilterRequest.sortByAndOrder + '-' + #searchFilterRequest.searchFilter")
    public PageDtoResponse<NewsDtoResponse> readAll(@Valid ResourceSearchFilterRequestDTO searchFilterRequest) {
        final ResourceSearchFilter searchFilter = newsSearchFilterMapper.map(searchFilterRequest);
        final Specification<News> specification = getEntitySearchSpecification(searchFilter).getSearchFilterSpecification();
        final Pageable pageable = createPageable(searchFilter);
        final Page<News> page = newsRepository.findAll(specification,pageable);
        final List<NewsDtoResponse> modelDtoList = mapper.modelListToDtoList(page.getContent());
        return new PageDtoResponse<>(modelDtoList, page.getNumber()+1, page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value="news",key="#id")
    public NewsDtoResponse readById(Long id) {
        return newsRepository.findById(id)
                .map(mapper::modelToDto)
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format(
                                        NEWS_ID_DOES_NOT_EXIST.getMessage(),
                                        id
                                )
                        )
                );
    }

    @Override
    @Transactional
    @Caching(evict={
            @CacheEvict(value="news", allEntries=true),
            @CacheEvict(value="newsPage", allEntries=true)
    })
    public NewsDtoResponse create(@Valid CreateNewsDtoRequest createRequest) {
        createNonExistentAuthor(createRequest.author());
        createNonExistentTags(createRequest.tags());
        try {
            News model = mapper.dtoToModel(createRequest);
            model = newsRepository.save(model);
            return mapper.modelToDto(model);
        } catch (EntityConflictRepositoryException exc) {
            throw new ResourceConflictServiceException(NEWS_CONFLICT.getMessage(), NEWS_CONFLICT.getErrorCode(), exc.getMessage());
        }
    }

    @Override
    @Transactional
    @Caching(evict={@CacheEvict(value="newsPage", allEntries=true)},
            put={@CachePut(value="news", key="#id")}
    )
    public NewsDtoResponse update(Long id, UpdateNewsDtoRequest updateRequest) {
        News existingNews = newsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(NEWS_ID_DOES_NOT_EXIST.getMessage(), id)
                ));

        createNonExistentAuthor(updateRequest.author());
        if (updateRequest.author() != null && !updateRequest.author().isBlank()){
            Author author = authorRepository.findByName(updateRequest.author())
                    .orElseThrow(() -> new NotFoundException("Author not found"));
            existingNews.setAuthor(author);
        }

        createNonExistentTags(updateRequest.tags());
        if (updateRequest.tags() != null && !updateRequest.tags().isEmpty()){
            List<Tag> tags = updateRequest.tags().stream()
                    .map(name -> tagRepository.findByName(name).orElseThrow())
                    .toList();
            existingNews.getTags().clear();
            existingNews.getTags().addAll(tags);
        }

        if (updateRequest.title() != null && !updateRequest.title().isBlank()) {
            existingNews.setTitle(updateRequest.title());
        }
        if (updateRequest.content() != null && !updateRequest.content().isBlank()) {
            existingNews.setContent(updateRequest.content());
        }

        News updated = newsRepository.save(existingNews);
        return mapper.modelToDto(updated);
    }

    @Override
    @Transactional
    @Caching(evict={
            @CacheEvict(value="news", key="#id"),
            @CacheEvict(value="newsPage", allEntries=true)
    })
    public void deleteById(Long id) {
        if (newsRepository.existsById(id)) {
            newsRepository.deleteById(id);
        } else {
            throw new NotFoundException(String.format(NEWS_ID_DOES_NOT_EXIST.getMessage(), id));
        }
    }

    private void createNonExistentAuthor(String authorName) {
        if (authorName != null && !authorName.isBlank()) {
            if (authorRepository.findByName(authorName).isEmpty()) {
                Author author = new Author();
                author.setName(authorName);
                authorRepository.save(author);
            }
        }
    }

    private void createNonExistentTags(List<String> tagNames) {
        if(tagNames !=null && !tagNames.isEmpty()){
            tagNames.stream()
                    .filter(name -> tagRepository.findByName(name).isEmpty())
                    .map(name -> {
                        Tag tag = new Tag();
                        tag.setName(name);
                        return tag;
                    })
                    .forEach(tagRepository::save);
        }

    }
}
