package com.mjc.school.repository.filter.specification;

import com.mjc.school.repository.filter.pagination.Pagination;
import com.mjc.school.repository.filter.sorting.Sorting;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class EntitySearchSpecification<T> {

    private final Pagination pagination;

    private final List<Sorting> sorting;

    private final Specification<T> searchFilterSpecification;

    private EntitySearchSpecification(Builder<T> builder) {
        this.pagination = builder.pagination;
        this.sorting = builder.sorting;
        this.searchFilterSpecification = builder.searchFilterSpecification;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public List<Sorting> getSorting() {
        return sorting;
    }

    public Specification<T> getSearchFilterSpecification() {
        return searchFilterSpecification;
    }

    public static class Builder<T> {
        private Pagination pagination;

        private List<Sorting> sorting;

        private Specification<T> searchFilterSpecification;

        public EntitySearchSpecification<T> build() {
            return new EntitySearchSpecification<>(this);
        }

        public Builder<T> pagination(final Pagination pagination) {
            this.pagination = pagination;
            return this;
        }

        public Builder<T> sorting(final List<Sorting> sorting) {
            this.sorting = sorting;
            return this;
        }

        public Builder<T> searchFilterSpecification(final List<SearchCriteria> searchCriteriaList) {
            this.searchFilterSpecification = new SearchFilterSpecificationsBuilder<T>()
                    .withSearchCriteriaList(searchCriteriaList).build();
            return this;
        }
    }
}
