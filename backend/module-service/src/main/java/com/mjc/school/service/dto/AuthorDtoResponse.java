package com.mjc.school.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorDtoResponse extends RepresentationModel<AuthorDtoResponse> {

    private Long id;
    private String name;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;

    public AuthorDtoResponse(){}

    public AuthorDtoResponse(Long id, String name, LocalDateTime createdDate, LocalDateTime lastUpdatedDate){
        this.id = id;
        this.name = name;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
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
        if (!(o instanceof AuthorDtoResponse that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(createdDate, that.createdDate) &&
                Objects.equals(lastUpdatedDate, that.lastUpdatedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, name, createdDate, lastUpdatedDate);
    }
}
