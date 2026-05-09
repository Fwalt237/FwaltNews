package com.mjc.school.service.dto;

import com.mjc.school.service.validator.constraint.Min;
import com.mjc.school.service.validator.constraint.SearchCriteria;
import com.mjc.school.service.validator.constraint.SortAndOrder;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;


public class ResourceSearchFilterRequestDTO {

    @Min(1)
    private int page = 1;

    private int pageSize = 10;


    @SortAndOrder
    private List<String> sortByAndOrder = new ArrayList<>();


    @SearchCriteria
    private List<String> searchCriteria = new ArrayList<>();

    public ResourceSearchFilterRequestDTO(
            @Nullable int page,
            @Nullable int pageSize,
            @Nullable List<String> sortByAndOrder,
            @Nullable List<String> searchCriteria) {
        if (page > 0) {
            this.page = page;
        }
        if (pageSize > 0) {
            this.pageSize = pageSize;
        }
        if (sortByAndOrder != null) {
            this.sortByAndOrder = sortByAndOrder;
        }
        if (searchCriteria != null) {
            this.searchCriteria = searchCriteria;
        }
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public List<String> getSortByAndOrder() {
        return sortByAndOrder;
    }

    public List<String> getSearchFilter() {
        return searchCriteria;
    }

}
