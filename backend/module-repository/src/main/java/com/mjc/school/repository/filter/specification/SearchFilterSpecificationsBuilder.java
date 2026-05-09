package com.mjc.school.repository.filter.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchFilterSpecificationsBuilder<T> {
    private final List<SearchCriteria> criteriaList;

    public SearchFilterSpecificationsBuilder() {
        criteriaList = new ArrayList<>();
    }

    public SearchFilterSpecificationsBuilder<T> withSearchCriteriaList(final List<SearchCriteria> searchCriteriaList) {
        if (!CollectionUtils.isEmpty(searchCriteriaList)) {
            criteriaList.addAll(searchCriteriaList);
        }
        return this;
    }

    public SearchFilterSpecificationsBuilder<T> withSearchCriteria(final SearchCriteria searchCriteria) {
        criteriaList.add(searchCriteria);
        return this;
    }

    public SearchFilterSpecificationsBuilder<T> with(final SearchOperation predicate, final String field, final SearchOperation operation, final Object value) {
        criteriaList.add(new SearchCriteria(predicate, field, operation, value));
        return this;
    }

    public Specification<T> build() {
        if (criteriaList.isEmpty()) {
            return null;
        }

        List<SearchFilterSpecification<T>> specs = criteriaList.stream()
                .map(SearchFilterSpecification<T>::new).toList();

        Specification<T> result = specs.get(0);

        for (int i = 1; i < criteriaList.size(); i++) {
            result = criteriaList.get(i)
                    .isOrPredicate()
                    ? Specification.where(result)
                    .or(specs.get(i))
                    : Specification.where(result)
                    .and(specs.get(i));
        }
        return result;
    }
}
