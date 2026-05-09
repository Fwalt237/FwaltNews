package com.mjc.school.controller.assembler;

import com.mjc.school.controller.impl.CommentController;
import com.mjc.school.controller.impl.NewsController;
import com.mjc.school.service.dto.CommentsDtoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;


@Component
public class CommentModelAssembler implements RepresentationModelAssembler<CommentsDtoResponse, CommentsDtoResponse> {

    private final LinkBuilderUtil linkBuilder;

    @Autowired
    public CommentModelAssembler(final LinkBuilderUtil linkBuilder){
        this.linkBuilder = linkBuilder;
    }

    @Override
    public CommentsDtoResponse toModel(CommentsDtoResponse comment) {

        comment.add(Link.of(linkBuilder.buildResourceLinkForMethod(
                CommentController.class,"readById",comment.getId()),"self"));

        comment.add(Link.of(linkBuilder.buildCollectionLink(CommentController.class),"comments"));

        if(comment.getNewsId()!=null){
            comment.add(Link.of(linkBuilder.buildResourceLink(NewsController.class,comment.getNewsId()),"news"));
        }

        comment.add(Link.of(linkBuilder.buildResourceLink(CommentController.class,comment.getId()),"update"));

        comment.add(Link.of(linkBuilder.buildResourceLink(CommentController.class,comment.getId()),"delete"));

        return comment;
    }

    @Override
    public CollectionModel<CommentsDtoResponse> toCollectionModel(Iterable<? extends CommentsDtoResponse> entities) {
        CollectionModel<CommentsDtoResponse> commentModels = RepresentationModelAssembler.super.toCollectionModel(entities);

        commentModels.add(Link.of(linkBuilder.buildCollectionLink(CommentController.class),"self"));
        commentModels.add(Link.of(linkBuilder.buildCollectionLink(CommentController.class),"create"));

        return commentModels;
    }
}
