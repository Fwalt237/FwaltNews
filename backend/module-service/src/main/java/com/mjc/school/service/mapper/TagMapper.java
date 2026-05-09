package com.mjc.school.service.mapper;

import com.mjc.school.repository.model.Tag;
import com.mjc.school.service.dto.TagDtoRequest;
import com.mjc.school.service.dto.TagDtoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Primary
@Mapper(componentModel = "spring",  nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS )
public interface TagMapper {

    List<TagDtoResponse> modelListToDtoList(List<Tag> modelList);

    TagDtoResponse modelToDto(Tag model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "news", ignore = true)
    Tag dtoToModel(TagDtoRequest dto);
}
