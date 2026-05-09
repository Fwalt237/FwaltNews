package com.mjc.school.controller.impl;

import com.mjc.school.controller.BaseController;
import com.mjc.school.controller.assembler.AuthorModelAssembler;
import com.mjc.school.controller.assembler.CommentModelAssembler;
import com.mjc.school.controller.assembler.LinkBuilderUtil;
import com.mjc.school.controller.assembler.NewsModelAssembler;
import com.mjc.school.controller.assembler.PageModelAssembler;
import com.mjc.school.controller.assembler.TagModelAssembler;
import com.mjc.school.service.BaseService;
import com.mjc.school.service.dto.AuthorDtoResponse;
import com.mjc.school.service.dto.CommentsDtoResponse;
import com.mjc.school.service.dto.CreateNewsDtoRequest;
import com.mjc.school.service.dto.NewsDtoResponse;
import com.mjc.school.service.dto.PageDtoResponse;
import com.mjc.school.service.dto.ResourceSearchFilterRequestDTO;
import com.mjc.school.service.dto.TagDtoResponse;
import com.mjc.school.service.dto.UpdateNewsDtoRequest;
import com.mjc.school.service.impl.AuthorService;
import com.mjc.school.service.impl.CommentService;
import com.mjc.school.service.impl.TagService;
import com.mjc.school.versioning.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
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


import static com.mjc.school.controller.RestApiConst.NEWS_API_ROOT_PATH;



@ApiVersion(1)
@RestController
@RequestMapping(value = NEWS_API_ROOT_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class NewsController
    implements BaseController<CreateNewsDtoRequest, NewsDtoResponse, Long, ResourceSearchFilterRequestDTO, UpdateNewsDtoRequest> {

    private final BaseService<CreateNewsDtoRequest, NewsDtoResponse, Long, ResourceSearchFilterRequestDTO, UpdateNewsDtoRequest> newsService;
    private final TagService tagService;
    private final AuthorService authorService;
    private final CommentService commentService;
    private final NewsModelAssembler newsAssembler;
    private final AuthorModelAssembler authorAssembler;
    private final TagModelAssembler tagAssembler;
    private final CommentModelAssembler commentAssembler;
    private final PageModelAssembler pageAssembler;
    private final LinkBuilderUtil linkBuilder;


    @Autowired
    public NewsController(
         final BaseService<CreateNewsDtoRequest, NewsDtoResponse, Long, ResourceSearchFilterRequestDTO, UpdateNewsDtoRequest> newsService,
         final TagService tagService,
         final AuthorService authorService,
         final CommentService commentService,
         final NewsModelAssembler newsAssembler,
         final AuthorModelAssembler authorAssembler,
         final TagModelAssembler tagAssembler,
         final CommentModelAssembler commentAssembler,
         final PageModelAssembler pageAssembler,
         final LinkBuilderUtil linkBuilder) {
        this.newsService = newsService;
        this.tagService = tagService;
        this.authorService = authorService;
        this.commentService = commentService;
        this.newsAssembler = newsAssembler;
        this.authorAssembler = authorAssembler;
        this.tagAssembler = tagAssembler;
        this.commentAssembler = commentAssembler;
        this.pageAssembler = pageAssembler;
        this.linkBuilder = linkBuilder;
    }

    @Operation(summary = "View all news", description = "Returns a paginated list of all news articles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all news"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @GetMapping
    public PageDtoResponse<NewsDtoResponse> readAll(
            final ResourceSearchFilterRequestDTO searchRequest) {
        PageDtoResponse<NewsDtoResponse> page = newsService.readAll(searchRequest);

        page.getModelDtoList().forEach(newsAssembler::toModel);

        String baseUrl = linkBuilder.buildCollectionLink(NewsController.class);
        return pageAssembler.addPaginationLinks(page,searchRequest,baseUrl);
    }

    @Operation(summary = "Retrieve a specific news article by ID", description = "Returns a single news article by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the news article"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @GetMapping("/{id}")
    public NewsDtoResponse readById(
            @PathVariable Long id) {
        NewsDtoResponse news = newsService.readById(id);
        return newsAssembler.toModel(news);
    }

    @Operation(summary = "Create a news article", description = "Adds a new news article to the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created the news article"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public NewsDtoResponse create(
            @RequestBody CreateNewsDtoRequest dtoRequest) {
        NewsDtoResponse news = newsService.create(dtoRequest);
        return newsAssembler.toModel(news);
    }

    @Operation(summary = "Update a news article", description = "Modifies an existing news article by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the news article"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public NewsDtoResponse update(
            @PathVariable Long id,
            @RequestBody UpdateNewsDtoRequest dtoRequest) {
        NewsDtoResponse news = newsService.update(id, dtoRequest);
        return newsAssembler.toModel(news);
    }


    @Operation(summary = "Delete a news article", description = "Removes a news article by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the news article"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(
            @PathVariable Long id) {
        newsService.deleteById(id);
    }


    @Operation(summary = "Get tags for a news article", description = "Returns all tags associated with a specific news article")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tags"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @GetMapping("/{id}/tags")
    public List<TagDtoResponse> readTagsByNewsId(
             @PathVariable Long id) {
        List<TagDtoResponse> tags = tagService.readByNewsId(id);
        tags.forEach(tagAssembler::toModel);
        return tags;
    }

    @Operation(summary = "Get author of a news article", description = "Returns the author information for a specific news article")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved author"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @GetMapping("/{id}/author")
    public AuthorDtoResponse readAuthorByNewsId(@PathVariable Long id) {
        AuthorDtoResponse author = authorService.readByNewsId(id);
        return authorAssembler.toModel(author);
    }

    @Operation(summary = "Get comments for a news article", description = "Returns all comments associated with a specific news article")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved comments"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    @GetMapping("/{id}/comments")
    public List<CommentsDtoResponse> readCommentsByNewsId(@PathVariable Long id) {
        List<CommentsDtoResponse> comments = commentService.readByNewsId(id);
        comments.forEach(commentAssembler::toModel);
        return comments;
    }
}
