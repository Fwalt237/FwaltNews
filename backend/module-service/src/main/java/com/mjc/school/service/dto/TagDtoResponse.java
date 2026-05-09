package com.mjc.school.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;
import org.springframework.hateoas.RepresentationModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagDtoResponse extends RepresentationModel<TagDtoResponse> {

    private Long id;
    private String name;

    public TagDtoResponse() {}

    public TagDtoResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagDtoResponse that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, name);
    }
}
