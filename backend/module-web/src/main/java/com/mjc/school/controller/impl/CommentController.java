package com.mjc.school.controller.impl;

import com.mjc.school.controller.BaseController;
import com.mjc.school.controller.assembler.CommentModelAssembler;
import com.mjc.school.controller.assembler.LinkBuilderUtil;
import com.mjc.school.controller.assembler.PageModelAssembler;
import com.mjc.school.service.BaseService;
import com.mjc.school.service.dto.CommentsDtoRequest;
import com.mjc.school.service.dto.CommentsDtoResponse;
import com.mjc.school.service.dto.PageDtoResponse;
import com.mjc.school.service.dto.ResourceSearchFilterRequestDTO;
import com.mjc.school.versioning.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


import static com.mjc.school.controller.RestApiConst.COMMENTS_API_ROOT_PATH;


@ApiVersion(1)
@RestController
@RequestMapping(value = COMMENTS_API_ROOT_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class CommentController
    implements BaseController<CommentsDtoRequest, CommentsDtoResponse, Long, ResourceSearchFilterRequestDTO, CommentsDtoRequest> {

    private final BaseService<CommentsDtoRequest, CommentsDtoResponse, Long, ResourceSearchFilterRequestDTO, CommentsDtoRequest> commentsService;
    private final CommentModelAssembler commentAssembler;
    private final PageModelAssembler pageAssembler;
    private final LinkBuilderUtil linkBuilder;

    @Autowired
    public CommentController(
            final BaseService<CommentsDtoRequest, CommentsDtoResponse, Long, ResourceSearchFilterRequestDTO, CommentsDtoRequest> commentsService,
            final CommentModelAssembler commentAssembler,
            final PageModelAssembler pageAssembler,
            final LinkBuilderUtil linkBuilder) {
        this.commentsService = commentsService;
        this.commentAssembler = commentAssembler;
        this.pageAssembler = pageAssembler;
        this.linkBuilder = linkBuilder;
    }

    @Operation(summary = "View all comments", description = "Returns a paginated list of all comments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all comments"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @GetMapping
    public PageDtoResponse<CommentsDtoResponse> readAll(
            final ResourceSearchFilterRequestDTO searchRequest) {
        PageDtoResponse<CommentsDtoResponse> page = commentsService.readAll(searchRequest);

        page.getModelDtoList().forEach(commentAssembler::toModel);

        String baseUrl = linkBuilder.buildCollectionLink(CommentController.class);
        return pageAssembler.addPaginationLinks(page,searchRequest,baseUrl);
    }

    @Operation(summary = "Retrieve a specific comment by ID", description = "Returns a single comment by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the comment"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @ApiVersion(2)
    @GetMapping("/{id}")
    public CommentsDtoResponse readById(@PathVariable Long id) {
        CommentsDtoResponse comment = commentsService.readById(id);
        return commentAssembler.toModel(comment);
    }


    @Operation(summary = "Create a new comment", description = "Adds a new comment to a news article")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created a comment"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CommentsDtoResponse create(@RequestBody CommentsDtoRequest dtoRequest) {
        CommentsDtoResponse comment = commentsService.create(dtoRequest);
        return commentAssembler.toModel(comment);
    }


    @Operation(summary = "Update an existing comment", description = "Modifies a comment's content by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the comment"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CommentsDtoResponse update(@PathVariable Long id,
            @RequestBody CommentsDtoRequest dtoRequest) {
        CommentsDtoResponse comment = commentsService.update(id, dtoRequest);
        return commentAssembler.toModel(comment);
    }



    @Operation(summary = "Delete a comment", description = "Removes a comment by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deletes the specific comment"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        commentsService.deleteById(id);
    }
}
