package com.mjc.school.service.filter.mapper;

import com.mjc.school.repository.filter.sorting.SortOrder;
import com.mjc.school.repository.filter.sorting.Sorting;
import com.mjc.school.service.dto.ResourceSearchFilterRequestDTO;
import com.mjc.school.service.filter.ResourceSearchFilter;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NewsSearchFilterMapper extends BaseSearchFilterMapper<ResourceSearchFilterRequestDTO> {

  @Override
  public List<Sorting> getDefaultSorting() {
    return List.of(new Sorting("title", SortOrder.ASC), new Sorting("createdDate", SortOrder.DESC));
  }

  @Override
  public ResourceSearchFilter map(ResourceSearchFilterRequestDTO searchFilterRequest) {
    return createResourceSearchFilter(
        searchFilterRequest.getPage(),
        searchFilterRequest.getPageSize(),
        searchFilterRequest.getSortByAndOrder(),
        searchFilterRequest.getSearchFilter());
  }
}
