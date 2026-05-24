package com.mjc.school.repository.filter.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.io.Serial;
import org.springframework.data.jpa.domain.Specification;

public class SearchFilterSpecification<T> implements Specification<T> {

  @Serial private static final long serialVersionUID = 1L;

  private final SearchCriteria criteria;

  public SearchFilterSpecification(SearchCriteria criteria) {
    this.criteria = criteria;
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    String field = criteria.getField();
    Object value = criteria.getValue();

    if ("keyword".equals(field)) {
      return buildGlobalKeywordPredicate(root, value, cb);
    }

    Path<?> path = parsePath(root, field);

    return criteria.getOperation().build(path, value, cb);
  }

  private Path<?> parsePath(Root<?> root, String field) {
    if (field.contains(".")) {
      String[] parts = field.split("\\.");
      return root.join(parts[0]).get(parts[1]);
    }
    return root.get(field);
  }

  private Predicate buildGlobalKeywordPredicate(Root<T> root, Object value, CriteriaBuilder cb) {
    String pattern = "%" + value.toString().toLowerCase() + "%";
    return cb.or(
        cb.like(cb.lower(root.get("title")), pattern),
        cb.like(cb.lower(root.get("content")), pattern));
  }
}
