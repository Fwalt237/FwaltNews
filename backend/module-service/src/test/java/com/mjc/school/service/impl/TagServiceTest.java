package com.mjc.school.service.impl;

import com.mjc.school.repository.filter.pagination.Pagination;
import com.mjc.school.repository.impl.TagRepository;
import com.mjc.school.repository.model.Tag;
import com.mjc.school.service.dto.PageDtoResponse;
import com.mjc.school.service.dto.ResourceSearchFilterRequestDTO;
import com.mjc.school.service.dto.TagDtoRequest;
import com.mjc.school.service.dto.TagDtoResponse;
import com.mjc.school.service.exceptions.NotFoundException;
import com.mjc.school.service.filter.ResourceSearchFilter;
import com.mjc.school.service.filter.mapper.TagSearchFilterMapper;
import com.mjc.school.service.mapper.TagMapper;
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
@DisplayName("Tag Service Unit tests")
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper mapper;

    @Mock
    private TagSearchFilterMapper tagSearchFilterMapper;

    @InjectMocks
    private TagService tagService;

    private Tag tag;
    private TagDtoRequest tagDtoRequest;
    private TagDtoResponse tagDtoResponse;

    @BeforeEach
    void setUp(){
        tag = new Tag();
        tag.setId(1L);
        tag.setName("Technology");

        tagDtoRequest = new TagDtoRequest("Technology");
        tagDtoResponse = new TagDtoResponse(1L,"Technology");
    }

    @Test
    @DisplayName("Should return all tags with pagination")
    void readAll_ShouldReturnPagedTags(){

        ResourceSearchFilterRequestDTO searchRequest = new ResourceSearchFilterRequestDTO(1,10, Collections.emptyList(),Collections.emptyList());
        Page<Tag> page = new PageImpl<>(List.of(tag));

        when(tagSearchFilterMapper.map(any())).thenReturn(new ResourceSearchFilter(new Pagination(1, 10),Collections.emptyList(),Collections.emptyList()));
        when(tagRepository.findAll(ArgumentMatchers.<Specification<Tag>>any(),any(Pageable.class))).thenReturn(page);
        when(mapper.modelListToDtoList(anyList())).thenReturn(List.of(tagDtoResponse));

        PageDtoResponse<TagDtoResponse> result = tagService.readAll(searchRequest);

        assertThat(result).isNotNull();
        assertThat(result.getModelDtoList()).hasSize(1);
        assertThat(result.getCurrentPage()).isEqualTo(1);
        assertThat(result.getPageCount()).isEqualTo(1);
        verify(tagRepository).findAll(ArgumentMatchers.<Specification<Tag>>any(),any(Pageable.class));
    }

    @Test
    @DisplayName("Should return tag ID when tag exists")
    void readById_WhenTagExists_ShouldReturnTag(){

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(mapper.modelToDto(tag)).thenReturn(tagDtoResponse);

        TagDtoResponse result = tagService.readById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Technology");
        verify(tagRepository).findById(1L);
    }


    @Test
    @DisplayName("Should throw NotFoundException when tag does not exist")
    void readById_WhenTagDoesNotExist_ShouldThrowNotFoundException(){

        when(tagRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(()->tagService.readById(2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Tag with id 2 does not exist.");

        verify(tagRepository).findById(2L);
    }


    @Test
    @DisplayName("Should create tag successfully")
    void create_ShouldCreateAndReturnTag(){

        when(mapper.dtoToModel(tagDtoRequest)).thenReturn(tag);
        when(tagRepository.save(tag)).thenReturn(tag);
        when(mapper.modelToDto(tag)).thenReturn(tagDtoResponse);

        TagDtoResponse result = tagService.create(tagDtoRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Technology");
        verify(tagRepository).save(tag);
    }

    @Test
    @DisplayName("Should update tag when tag exists")
    void update_WhenTagExists_ShouldUpdateTag(){

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.save(tag)).thenReturn(tag);
        when(mapper.modelToDto(tag)).thenReturn(tagDtoResponse);

        TagDtoResponse result = tagService.update(1L,tagDtoRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(tagRepository).save(tag);
    }

    @Test
    @DisplayName("Should throw NotFoundException when updating non-existent tag")
    void update_WhenTagDoesNotExist_ShouldThrowNotFoundException(){

        when(tagRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(()->tagService.update(2L,tagDtoRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Tag with id 2 does not exist.");

        verify(tagRepository,never()).save(any());
    }


    @Test
    @DisplayName("Should delete tag when tag exists")
    void deleteById_WhenTagExists_ShouldDeleteTag(){

        when(tagRepository.existsById(1L)).thenReturn(true);
        doNothing().when(tagRepository).deleteById(1L);

        tagService.deleteById(1L);

        verify(tagRepository).deleteById(1L);
    }


    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent tag")
    void deleteById_WhenTagDoesNotExist_ShouldThrowNotFoundException(){

        when(tagRepository.existsById(2L)).thenReturn(false);

        assertThatThrownBy(()->tagService.deleteById(2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Tag with id 2 does not exist.");

        verify(tagRepository,never()).deleteById(any());
    }


    @Test
    @DisplayName("Should return tags by news ID")
    void readByNewsId_WhenTagExists_ShouldReturnTag(){

        when(tagRepository.findByNewsId(1L)).thenReturn(List.of(tag));
        when(mapper.modelListToDtoList(List.of(tag))).thenReturn(List.of(tagDtoResponse));

        List<TagDtoResponse> result = tagService.readByNewsId(1L);

        assertThat(result).isNotNull().hasSizeGreaterThan(0);
        verify(tagRepository).findByNewsId(1L);
    }
}
