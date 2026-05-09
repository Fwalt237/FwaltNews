package com.mjc.school.service.impl;

import com.mjc.school.repository.exception.EntityConflictRepositoryException;
import com.mjc.school.repository.impl.TagRepository;
import com.mjc.school.repository.model.Tag;
import com.mjc.school.service.BaseService;
import com.mjc.school.service.dto.PageDtoResponse;
import com.mjc.school.service.dto.ResourceSearchFilterRequestDTO;
import com.mjc.school.service.dto.TagDtoRequest;
import com.mjc.school.service.dto.TagDtoResponse;
import com.mjc.school.service.exceptions.NotFoundException;
import com.mjc.school.service.exceptions.ResourceConflictServiceException;
import com.mjc.school.service.filter.ResourceSearchFilter;
import com.mjc.school.service.filter.mapper.TagSearchFilterMapper;
import com.mjc.school.service.mapper.TagMapper;
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


import static com.mjc.school.service.exceptions.ServiceErrorCode.TAG_CONFLICT;
import static com.mjc.school.service.exceptions.ServiceErrorCode.TAG_ID_DOES_NOT_EXIST;

@Service
public class TagService implements
    BaseService<TagDtoRequest, TagDtoResponse, Long, ResourceSearchFilterRequestDTO, TagDtoRequest> {

    private final TagRepository tagRepository;
    private final TagMapper mapper;
    private final TagSearchFilterMapper tagSearchFilterMapper;

    @Autowired
    public TagService(TagRepository tagRepository, TagMapper mapper, TagSearchFilterMapper tagSearchFilterMapper) {
        this.tagRepository = tagRepository;
        this.mapper = mapper;
        this.tagSearchFilterMapper = tagSearchFilterMapper;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tags",
            key = "'page-' + #searchFilterRequest.page + '-' + #searchFilterRequest.pageSize")
    public PageDtoResponse<TagDtoResponse> readAll(@Valid ResourceSearchFilterRequestDTO searchFilterRequest) {
        final ResourceSearchFilter searchFilter = tagSearchFilterMapper.map(searchFilterRequest);
        final Specification<Tag> specification = getEntitySearchSpecification(searchFilter).getSearchFilterSpecification();
        final Pageable pageable = createPageable(searchFilter);
        final Page<Tag> page = tagRepository.findAll(specification,pageable);
        final List<TagDtoResponse> modelDtoList = mapper.modelListToDtoList(page.getContent());
        return new PageDtoResponse<>(modelDtoList, page.getNumber()+1, page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tags", key = "#id")
    public TagDtoResponse readById(Long id) {
        return tagRepository.findById(id)
            .map(mapper::modelToDto)
            .orElseThrow(
                () -> new NotFoundException(
                    String.format(
                        TAG_ID_DOES_NOT_EXIST.getMessage(),
                        id
                    )
                )
            );
    }

    @Override
    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public TagDtoResponse create(@Valid TagDtoRequest createRequest) {
        try {
            Tag model = mapper.dtoToModel(createRequest);
            model = tagRepository.save(model);
            return mapper.modelToDto(model);
        } catch (EntityConflictRepositoryException exc) {
            throw new ResourceConflictServiceException(TAG_CONFLICT.getMessage(), TAG_CONFLICT.getErrorCode(), exc.getMessage());
        }
    }

    @Override
    @Transactional
    @Caching(
            put  = { @CachePut(value = "tags", key = "#id") },
            evict = { @CacheEvict(value = "tags", allEntries = true) }
    )
    public TagDtoResponse update(Long id, @Valid TagDtoRequest updateRequest) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(()->new NotFoundException(String.format(TAG_ID_DOES_NOT_EXIST.getMessage(), id)));

        if(updateRequest.name()!=null && !updateRequest.name().isBlank()){
            tag.setName(updateRequest.name());
        }
        Tag updatedTag = tagRepository.save(tag);
        return mapper.modelToDto(updatedTag);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public void deleteById(Long id) {
        if (tagRepository.existsById(id)) {
            tagRepository.deleteById(id);
        } else {
            throw new NotFoundException(String.format(TAG_ID_DOES_NOT_EXIST.getMessage(), id));
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tags", key = "'byNews-' + #newsId")
    public List<TagDtoResponse> readByNewsId(Long newsId) {
        return mapper.modelListToDtoList(tagRepository.findByNewsId(newsId));
    }
}
