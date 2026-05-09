
package com.mjc.school.controller.assembler;

import com.mjc.school.controller.impl.AuthorController;
import com.mjc.school.service.dto.AuthorDtoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class AuthorModelAssembler implements RepresentationModelAssembler<AuthorDtoResponse, AuthorDtoResponse> {

    private final LinkBuilderUtil linkBuilder;

    @Autowired
    public AuthorModelAssembler(final LinkBuilderUtil linkBuilder){
        this.linkBuilder = linkBuilder;
    }

    @Override
    public AuthorDtoResponse toModel(AuthorDtoResponse author) {

        author.add(Link.of(linkBuilder.buildResourceLinkForMethod(
                AuthorController.class,"readById",author.getId()),"self"));

        author.add(Link.of(linkBuilder.buildCollectionLink(AuthorController.class),"authors"));

        author.add(Link.of(linkBuilder.buildResourceLink(AuthorController.class,author.getId()),"update"));

        author.add(Link.of(linkBuilder.buildResourceLink(AuthorController.class,author.getId()),"delete"));

        return author;
    }

    @Override
    public CollectionModel<AuthorDtoResponse> toCollectionModel(Iterable<? extends AuthorDtoResponse> entities) {
        CollectionModel<AuthorDtoResponse>  authorModels = RepresentationModelAssembler.super.toCollectionModel(entities);

        authorModels.add(Link.of(linkBuilder.buildCollectionLink(AuthorController.class),"self"));
        authorModels.add(Link.of(linkBuilder.buildCollectionLink(AuthorController.class),"create"));

        return authorModels;
    }
}