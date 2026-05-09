package com.mjc.school.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentsDtoResponse extends RepresentationModel<CommentsDtoResponse> {

    private Long id;
    private String content;
    private Long newsId;
    private String authorName;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;

    public CommentsDtoResponse() {
    }

    public CommentsDtoResponse(Long id, String content, Long newsId, String authorName, LocalDateTime createdDate, LocalDateTime lastUpdatedDate) {
        this.id = id;
        this.content = content;
        this.newsId = newsId;
        this.authorName = authorName;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Long getNewsId() {
        return newsId;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setNewsId(Long newsId) {
        this.newsId = newsId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastUpdatedDate(LocalDateTime lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommentsDtoResponse that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(id, that.id) &&
                Objects.equals(content, that.content) &&
                Objects.equals(newsId, that.newsId) &&
                Objects.equals(authorName, that.authorName) &&
                Objects.equals(createdDate, that.createdDate) &&
                Objects.equals(lastUpdatedDate, that.lastUpdatedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, content, newsId, authorName, createdDate, lastUpdatedDate);
    }
}

