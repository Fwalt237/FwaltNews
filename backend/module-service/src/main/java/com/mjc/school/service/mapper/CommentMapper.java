package com.mjc.school.service.mapper;

import com.mjc.school.repository.impl.NewsRepository;
import com.mjc.school.repository.model.Comment;
import com.mjc.school.service.dto.CommentsDtoForNewsResponse;
import com.mjc.school.service.dto.CommentsDtoRequest;
import com.mjc.school.service.dto.CommentsDtoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Primary
@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class CommentMapper {

    @Autowired
    protected NewsRepository newsRepository;
    public abstract List<CommentsDtoResponse> modelListToDtoList(List<Comment> modelList);

    @Mapping(target = "newsId", expression = "java(model.getNews().getId())")
    @Mapping(target = "authorName", expression = "java(model.getUser() != null ? model.getUser().getUsername() : \"Anonymous\")")
    public abstract CommentsDtoResponse modelToDto(Comment model);

    public abstract CommentsDtoForNewsResponse modelToDtoForNews(Comment model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastUpdatedDate", ignore = true)
    @Mapping(target = "news", expression = "java(newsRepository.getReferenceById(dto.newsId()))")
    public abstract Comment dtoToModel(CommentsDtoRequest dto);
}
