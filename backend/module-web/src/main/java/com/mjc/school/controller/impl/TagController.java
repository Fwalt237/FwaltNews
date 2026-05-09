package com.mjc.school.controller.impl;

import com.mjc.school.controller.BaseController;
import com.mjc.school.controller.assembler.LinkBuilderUtil;
import com.mjc.school.controller.assembler.PageModelAssembler;
import com.mjc.school.controller.assembler.TagModelAssembler;
import com.mjc.school.service.BaseService;
import com.mjc.school.service.dto.PageDtoResponse;
import com.mjc.school.service.dto.ResourceSearchFilterRequestDTO;
import com.mjc.school.service.dto.TagDtoRequest;
import com.mjc.school.service.dto.TagDtoResponse;
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
import static com.mjc.school.controller.RestApiConst.TAG_API_ROOT_PATH;

@ApiVersion(1)
@RestController
@RequestMapping(value = TAG_API_ROOT_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class TagController
    implements BaseController<TagDtoRequest, TagDtoResponse, Long, ResourceSearchFilterRequestDTO, TagDtoRequest> {

    private final BaseService<TagDtoRequest, TagDtoResponse, Long, ResourceSearchFilterRequestDTO, TagDtoRequest> tagService;
    private final TagModelAssembler tagAssembler;
    private final PageModelAssembler pageAssembler;
    private final LinkBuilderUtil linkBuilder;

    @Autowired
    public TagController(final BaseService<TagDtoRequest, TagDtoResponse, Long, ResourceSearchFilterRequestDTO, TagDtoRequest> tagService,
                         final TagModelAssembler tagAssembler,
                         final PageModelAssembler pageAssembler,
                         final LinkBuilderUtil linkBuilder) {
        this.tagService = tagService;
        this.tagAssembler = tagAssembler;
        this.pageAssembler = pageAssembler;
        this.linkBuilder = linkBuilder;
    }


    @Operation(summary = "View all tags", description = "Returns a paginated list of all tags")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all tags"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDtoResponse<TagDtoResponse> readAll(ResourceSearchFilterRequestDTO searchRequest) {
        PageDtoResponse<TagDtoResponse> page = tagService.readAll(searchRequest);

        page.getModelDtoList().forEach(tagAssembler::toModel);

        String baseUrl = linkBuilder.buildCollectionLink(TagController.class);
        return pageAssembler.addPaginationLinks(page,searchRequest,baseUrl);
    }

    @Operation(summary = "Retrieve a specific tag by ID", description = "Returns a single tag by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the tag"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @ApiVersion(2)
    @GetMapping("/{id}")
    public TagDtoResponse readById(@PathVariable Long id) {
        TagDtoResponse tag = tagService.readById(id);
        return tagAssembler.toModel(tag);
    }


    @Operation(summary = "Create a new tag", description = "Adds a new tag to the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created the tag"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })

    @Override
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public TagDtoResponse create(@RequestBody TagDtoRequest dtoRequest) {
        TagDtoResponse tag = tagService.create(dtoRequest);
        return tagAssembler.toModel(tag);
    }



    @Operation(summary = "Update an existing tag", description = "Modifies a tag's details by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the tag"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TagDtoResponse update(@PathVariable Long id,
            @RequestBody TagDtoRequest dtoRequest) {
        TagDtoResponse tag = tagService.update(id, dtoRequest);
        return tagAssembler.toModel(tag);
    }

    @Operation(summary = "Delete a tag", description = "Removes a tag by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the tag"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
         tagService.deleteById(id);
    }
}
