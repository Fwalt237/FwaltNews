package com.mjc.school.controller.assembler;

import com.mjc.school.controller.impl.AuthorController;
import com.mjc.school.controller.impl.NewsController;
import com.mjc.school.service.dto.NewsDtoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;


@Component
public class NewsModelAssembler implements RepresentationModelAssembler<NewsDtoResponse, NewsDtoResponse> {

    private final AuthorModelAssembler authorAssembler;
    private final TagModelAssembler tagAssembler;
    private final LinkBuilderUtil linkBuilder;

    @Autowired
    public NewsModelAssembler(final LinkBuilderUtil linkBuilder,final AuthorModelAssembler authorAssembler,final TagModelAssembler tagAssembler){
        this.authorAssembler = authorAssembler;
        this.tagAssembler = tagAssembler;
        this.linkBuilder = linkBuilder;
    }

    @Override
    public NewsDtoResponse toModel(NewsDtoResponse news) {

        news.add(Link.of(linkBuilder.buildResourceLink(
                AuthorController.class,news.getId()),"self"));

        news.add(Link.of(linkBuilder.buildCollectionLink(NewsController.class),"news"));

        news.add(Link.of(linkBuilder.buildResourceLink(NewsController.class,news.getId()),"update"));

        news.add(Link.of(linkBuilder.buildResourceLink(NewsController.class,news.getId()),"delete"));

        news.add(Link.of(linkBuilder.buildNestedResourceLink(NewsController.class,news.getId(),"author"),"author"));
        news.add(Link.of(linkBuilder.buildNestedResourceLink(NewsController.class,news.getId(),"tags"),"tags"));
        news.add(Link.of(linkBuilder.buildNestedResourceLink(NewsController.class,news.getId(),"comments"),"comments"));

        if(news.getAuthorDto()!=null){
            authorAssembler.toModel(news.getAuthorDto());
        }
        if(news.getTagsDto()!=null){
            news.getTagsDto().forEach(tagAssembler::toModel);
        }

        return news;
    }

    @Override
    public CollectionModel<NewsDtoResponse> toCollectionModel(Iterable<? extends NewsDtoResponse> entities) {
        CollectionModel<NewsDtoResponse> newsModels =  RepresentationModelAssembler.super.toCollectionModel(entities);

        newsModels.add(Link.of(linkBuilder.buildCollectionLink(NewsController.class),"self"));
        newsModels.add(Link.of(linkBuilder.buildCollectionLink(NewsController.class),"create"));

        return newsModels;
    }
}
