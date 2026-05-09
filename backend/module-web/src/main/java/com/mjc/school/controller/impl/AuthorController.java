package com.mjc.school.controller.impl;

import com.mjc.school.controller.BaseController;
import com.mjc.school.controller.assembler.AuthorModelAssembler;
import com.mjc.school.controller.assembler.LinkBuilderUtil;
import com.mjc.school.controller.assembler.PageModelAssembler;
import com.mjc.school.service.BaseService;
import com.mjc.school.service.dto.AuthorDtoRequest;
import com.mjc.school.service.dto.AuthorDtoResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


import static com.mjc.school.controller.RestApiConst.AUTHOR_API_ROOT_PATH;


@ApiVersion(1)
@RestController
@RequestMapping(value = AUTHOR_API_ROOT_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthorController
    implements BaseController<AuthorDtoRequest, AuthorDtoResponse, Long, ResourceSearchFilterRequestDTO, AuthorDtoRequest> {

    private final BaseService<AuthorDtoRequest, AuthorDtoResponse, Long, ResourceSearchFilterRequestDTO, AuthorDtoRequest> authorService;
    private final AuthorModelAssembler authorAssembler;
    private final PageModelAssembler pageAssembler;
    private final LinkBuilderUtil linkBuilder;

    @Autowired
    public AuthorController(
        final BaseService<AuthorDtoRequest, AuthorDtoResponse, Long, ResourceSearchFilterRequestDTO, AuthorDtoRequest> authorService
    ,final AuthorModelAssembler authorAssembler,final PageModelAssembler pageAssembler,final LinkBuilderUtil linkBuilder) {
        this.authorService = authorService;
        this.authorAssembler=authorAssembler;
        this.pageAssembler=pageAssembler;
        this.linkBuilder = linkBuilder;
    }

    @Operation(summary = "View all authors", description = "Returns a paginated list of all authors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all authors"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @GetMapping
    public PageDtoResponse<AuthorDtoResponse> readAll(
            final ResourceSearchFilterRequestDTO searchRequest) {
        PageDtoResponse<AuthorDtoResponse> page = authorService.readAll(searchRequest);

        page.getModelDtoList().forEach(authorAssembler::toModel);

        String baseUrl = linkBuilder.buildCollectionLink(AuthorController.class);
        return pageAssembler.addPaginationLinks(page,searchRequest,baseUrl);
    }

    @Operation(summary = "Retrieve a specific author by ID", description = "Returns a single author by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the author"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @ApiVersion(2)
    @GetMapping("/{id}")
    public AuthorDtoResponse readById(@PathVariable Long id) {
        AuthorDtoResponse author = authorService.readById(id);
        return authorAssembler.toModel(author);
    }


    @Operation(summary = "Create a new author", description = "Adds a new author to the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created an author"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorDtoResponse create(@RequestBody AuthorDtoRequest dtoRequest) {
        AuthorDtoResponse author =  authorService.create(dtoRequest);
        return authorAssembler.toModel(author);
    }


    @Operation(summary = "Update an existing author", description = "Modifies an author's details by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated author"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AuthorDtoResponse update(@PathVariable Long id,
            @RequestBody AuthorDtoRequest dtoRequest) {
        AuthorDtoResponse author =  authorService.update(id, dtoRequest);
        return authorAssembler.toModel(author);
    }


    @Operation(summary = "Delete an author", description = "Removes an author by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deletes the specific author"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        authorService.deleteById(id);
    }
}
