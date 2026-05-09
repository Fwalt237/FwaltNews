package com.mjc.school.repository.filter.specification;

import java.io.Serial;
import java.io.Serializable;


import static com.mjc.school.repository.filter.specification.SearchOperation.OR_PREDICATE;

public class SearchCriteria implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    final private SearchOperation predicate;
    final private String field;
    final private SearchOperation operation;
    final private Object value;

    public SearchCriteria(final SearchOperation predicate, final String field, final SearchOperation operation, final Object value) {
        this.predicate = predicate;
        this.field = field;
        this.operation = operation;
        this.value = value;
    }

    public SearchCriteria(final String field, final SearchOperation operation, final Object value) {
        this(null, field, operation, value);
    }

    public SearchOperation getPredicate() {
        return predicate;
    }

    public String getField() {
        return field;
    }

    public SearchOperation getOperation() {
        return operation;
    }

    public Object getValue() {
        return value;
    }

    public boolean isOrPredicate() {
        return OR_PREDICATE.equals(predicate);
    }
}
