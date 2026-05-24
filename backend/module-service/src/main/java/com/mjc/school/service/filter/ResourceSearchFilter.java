package com.mjc.school.service.filter;

import com.mjc.school.repository.filter.pagination.Pagination;
import com.mjc.school.repository.filter.sorting.Sorting;
import com.mjc.school.repository.filter.specification.SearchCriteria;
import java.util.List;

public class ResourceSearchFilter {

  private final Pagination pagination;

  private final List<Sorting> sorting;

  private final List<SearchCriteria> searchCriteriaList;

  public ResourceSearchFilter(
      final Pagination pagination,
      final List<Sorting> sorting,
      final List<SearchCriteria> searchCriteriaList) {
    this.pagination = pagination;
    this.sorting = sorting;
    this.searchCriteriaList = searchCriteriaList;
  }

  public Pagination getPagination() {
    return pagination;
  }

  public List<Sorting> getOrder() {
    return sorting;
  }

  public List<SearchCriteria> getSearchCriteriaList() {
    return searchCriteriaList;
  }
}
