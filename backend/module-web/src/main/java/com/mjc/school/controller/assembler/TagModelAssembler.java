package com.mjc.school.controller.assembler;

import com.mjc.school.controller.impl.TagController;
import com.mjc.school.service.dto.TagDtoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class TagModelAssembler implements RepresentationModelAssembler<TagDtoResponse,TagDtoResponse> {

    private final LinkBuilderUtil linkBuilder;

    @Autowired
    public TagModelAssembler(final LinkBuilderUtil linkBuilder){
        this.linkBuilder = linkBuilder;
    }

    @Override
    public TagDtoResponse toModel(TagDtoResponse tag) {

        tag.add(Link.of(linkBuilder.buildResourceLinkForMethod(
                TagController.class,"readById",tag.getId()),"self"));

        tag.add(Link.of(linkBuilder.buildCollectionLink(TagController.class),"tags"));

        tag.add(Link.of(linkBuilder.buildResourceLink(TagController.class,tag.getId()),"update"));

        tag.add(Link.of(linkBuilder.buildResourceLink(TagController.class,tag.getId()),"delete"));

        return tag;
    }

    @Override
    public CollectionModel<TagDtoResponse> toCollectionModel(Iterable<? extends TagDtoResponse> entities) {

        CollectionModel<TagDtoResponse> tagModels = RepresentationModelAssembler.super.toCollectionModel(entities);

        tagModels.add(Link.of(linkBuilder.buildCollectionLink(TagController.class),"self"));
        tagModels.add(Link.of(linkBuilder.buildCollectionLink(TagController.class),"create"));

        return tagModels;
    }
}
