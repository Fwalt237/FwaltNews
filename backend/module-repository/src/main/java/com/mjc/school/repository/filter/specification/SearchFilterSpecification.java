package com.mjc.school.repository.filter.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.io.Serial;
import org.springframework.data.jpa.domain.Specification;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public class SearchFilterSpecification<T> implements Specification<T> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final SearchCriteria criteria;

    public SearchFilterSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    public Class<T> getType() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        return (Class<T>) type.getActualTypeArguments()[0];
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        String field = criteria.getField();
        Object value = criteria.getValue();
        SearchOperation op = criteria.getOperation();

        if ("keyword".equals(field)) {
            String pattern = "%" + value.toString().toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), pattern)
            );
        }

        Path<?> path;
        if (field.contains(".")) {
            String[] parts = field.split("\\.");
            path = root.join(parts[0]).get(parts[1]);
        } else {
            path = root.get(field);
        }

        if (op.equals(SearchOperation.EQUAL)) {
            return criteriaBuilder.equal(path, value);
        }
        else if (op.equals(SearchOperation.NOT_EQUAL)) {
            return criteriaBuilder.notEqual(path, value);
        }
        else if (op.equals(SearchOperation.LIKE)) {
            return criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)),
                    "%" + value.toString().toLowerCase() + "%");
        }
        else if (op.equals(SearchOperation.LIKE_START)) {
            return criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)),
                    value.toString().toLowerCase() + "%");
        }
        else if (op.equals(SearchOperation.LIKE_END)) {
            return criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)),
                    "%" + value.toString().toLowerCase());
        }
        else if (op.equals(SearchOperation.GREATER_THAN)) {
            return criteriaBuilder.greaterThan(path.as(String.class), value.toString());
        }
        else if (op.equals(SearchOperation.GREATER_THAN_EQUAL)) {
            return criteriaBuilder.greaterThanOrEqualTo(path.as(String.class), value.toString());
        }
        else if (op.equals(SearchOperation.LESS_THAN)) {
            return criteriaBuilder.lessThan(path.as(String.class), value.toString());
        }
        else if (op.equals(SearchOperation.LESS_THAN_EQUAL)) {
            return criteriaBuilder.lessThanOrEqualTo(path.as(String.class), value.toString());
        }
        else if (op.equals(SearchOperation.IN)) {
            return path.in(value);
        }
        else if (op.equals(SearchOperation.NOT_IN)) {
            return criteriaBuilder.not(path.in(value));
        }
        else if (op.equals(SearchOperation.BETWEEN) && value instanceof List) {
            List<String> values = (List<String>) value;
            return criteriaBuilder.between(path.as(String.class), values.get(0), values.get(1));
        }

        return null;
    }
}
