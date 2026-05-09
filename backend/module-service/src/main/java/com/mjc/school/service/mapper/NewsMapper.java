package com.mjc.school.service.mapper;

import com.mjc.school.repository.impl.AuthorRepository;
import com.mjc.school.repository.impl.CommentRepository;
import com.mjc.school.repository.impl.TagRepository;
import com.mjc.school.repository.model.News;
import com.mjc.school.service.dto.CommentsDtoForNewsResponse;
import com.mjc.school.service.dto.CreateNewsDtoRequest;
import com.mjc.school.service.dto.NewsDtoResponse;
import com.mjc.school.service.dto.UpdateNewsDtoRequest;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Hibernate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;

@Primary
@Mapper(componentModel = "spring", uses = {AuthorMapper.class, TagMapper.class, CommentMapper.class},
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class NewsMapper {

    @Autowired
    protected AuthorRepository authorRepository;
    @Autowired
    protected TagRepository tagRepository;
    @Autowired
    protected CommentRepository commentsRepository;
    @Autowired
    protected CommentMapper commentMapper;

    public abstract List<NewsDtoResponse> modelListToDtoList(List<News> modelList);

    @Mapping(source = "author", target = "authorDto")
    @Mapping(source = "tags", target = "tagsDto")
    @Mapping(target = "commentsDto", ignore = true)
    public abstract NewsDtoResponse modelToDto(News model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastUpdatedDate", ignore = true)
    @Mapping(target = "author", expression =
            "java(authorRepository.findByName(dto.author()).get())")
    @Mapping(target = "tags", expression =
            "java(dto.tags().stream().map(name -> tagRepository.findByName(name).get()).toList())")
    @Mapping(target = "comments", expression =
            "java(dto.commentsIds().stream().map(commentId -> commentsRepository.getReferenceById(commentId)).toList())")
    public abstract News dtoToModel(CreateNewsDtoRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastUpdatedDate", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "comments", expression =
            "java(dto.commentsIds().stream().map(commentId -> commentsRepository.getReferenceById(commentId)).toList())")
    public abstract News dtoToModel(UpdateNewsDtoRequest dto);


    @AfterMapping
    void setComments(News model, @MappingTarget NewsDtoResponse dto) {
        if (Hibernate.isInitialized(model.getComments()) && model.getComments() != null) {
            List<CommentsDtoForNewsResponse> commentsDto = model.getComments().stream()
                    .map(c -> commentMapper.modelToDtoForNews(c))
                    .toList();
            dto.setCommentsDto(commentsDto);
        } else {
            dto.setCommentsDto(new ArrayList<>());
        }
    }
}
