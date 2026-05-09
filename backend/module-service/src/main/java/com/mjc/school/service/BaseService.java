package com.mjc.school.service;

import com.mjc.school.repository.filter.sorting.SortOrder;
import com.mjc.school.repository.filter.specification.EntitySearchSpecification;
import com.mjc.school.service.dto.PageDtoResponse;
import com.mjc.school.service.filter.ResourceSearchFilter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface BaseService<C, R, G, S, U> {
    PageDtoResponse<R> readAll(S searchFilterRequest);

    R readById(G id);

    R create(C createRequest);

    R update(G id, U updateRequest);

    void deleteById(G id);

    default EntitySearchSpecification getEntitySearchSpecification(final ResourceSearchFilter searchFilter) {
        return new EntitySearchSpecification.Builder()
                .pagination(searchFilter.getPagination())
                .sorting(searchFilter.getOrder())
                .searchFilterSpecification(searchFilter.getSearchCriteriaList()).build();
    }

    default Pageable createPageable(ResourceSearchFilter searchFilter){
        int page = searchFilter.getPagination().page() -1;
        int size = searchFilter.getPagination().pageSize();

        Sort sort = Sort.unsorted();

        if(searchFilter.getOrder() !=null && !searchFilter.getOrder().isEmpty()){
            Sort.Order[] orders = searchFilter.getOrder().stream()
                    .map(sorting ->{
                        Sort.Direction direction = sorting.order() == SortOrder.ASC
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;
                        return new Sort.Order(direction,sorting.field());
                    }).toArray(Sort.Order[]::new);
            sort = Sort.by(orders);
        }
        return PageRequest.of(page,size,sort);
    }

}
