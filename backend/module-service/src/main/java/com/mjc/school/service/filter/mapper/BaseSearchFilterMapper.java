package com.mjc.school.service.filter.mapper;

import com.mjc.school.repository.filter.pagination.Pagination;
import com.mjc.school.repository.filter.sorting.SortOrder;
import com.mjc.school.repository.filter.sorting.Sorting;
import com.mjc.school.repository.filter.specification.SearchCriteria;
import com.mjc.school.repository.filter.specification.SearchOperation;
import com.mjc.school.service.filter.ResourceSearchFilter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.CollectionUtils;

public abstract class BaseSearchFilterMapper<T> {

    public final static String SORT_AND_FILTER_DELIMITER = ":";

    public abstract List<Sorting> getDefaultSorting();

    public abstract ResourceSearchFilter map(T searchFilterRequest);

    protected ResourceSearchFilter createResourceSearchFilter(final int page, final int pageSize, final List<String> sortAndOrder, final List<String> searchFilterRequest) {
        Pagination pagination = new Pagination(page, pageSize);
        List<Sorting> sorting = createSorting(sortAndOrder);
        List<SearchCriteria> searchCriteriaList = createSearchCriteriaList(searchFilterRequest);
        return new ResourceSearchFilter(pagination, sorting, searchCriteriaList);
    }

    protected List<Sorting> createSorting(List<String> sorting) {
        List<Sorting> sortingList = new ArrayList<>();
        if (CollectionUtils.isEmpty(sorting)) {
            return getDefaultSorting();
        }
        for (String sort : sorting) {
            String[] splitSorting = sort.split(SORT_AND_FILTER_DELIMITER);
            sortingList.add(new Sorting(splitSorting[0], SortOrder.valueOf(splitSorting[1].toUpperCase())));
        }
        return sortingList;
    }

    protected List<SearchCriteria> createSearchCriteriaList(final List<String> searchFilter) {
        List<SearchCriteria> searchCriteriaList = new ArrayList<>();
        if (CollectionUtils.isEmpty(searchFilter)) {
            return List.of();
        }
        for (String filter : searchFilter) {
            String[] splitFilter = filter.split(SORT_AND_FILTER_DELIMITER);
            if (splitFilter.length == 3) {
                searchCriteriaList.add(
                    new SearchCriteria(splitFilter[0], SearchOperation.getSearchOperationByName(splitFilter[1]), splitFilter[2]));
            }
        }
        return searchCriteriaList;
    }
}
