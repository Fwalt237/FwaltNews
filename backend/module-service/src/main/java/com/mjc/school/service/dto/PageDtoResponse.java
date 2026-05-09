package com.mjc.school.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;
import org.springframework.hateoas.RepresentationModel;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageDtoResponse<T> extends RepresentationModel<PageDtoResponse<T>> {

    private List<T> modelDtoList;
    private int currentPage;
    private int pageCount;

    public PageDtoResponse() {
        this.modelDtoList = new ArrayList<>();
    }

    public PageDtoResponse(List<T> modelDtoList, int currentPage, int pageCount) {
        this.modelDtoList = modelDtoList !=null ? modelDtoList: new ArrayList<>();
        this.currentPage = currentPage;
        this.pageCount = pageCount;
    }

    public List<T> getModelDtoList() {
        return modelDtoList;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setModelDtoList(List<T> modelDtoList) {
        this.modelDtoList = modelDtoList;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageDtoResponse<?> that)) return false;
        if (!super.equals(o)) return false;
        return currentPage == that.currentPage &&
                pageCount == that.pageCount &&
                Objects.equals(modelDtoList, that.modelDtoList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), modelDtoList, currentPage, pageCount);
    }
}
