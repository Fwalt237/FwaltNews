package com.mjc.school.repository.filter.specification;

import com.mjc.school.repository.exception.SearchOperationNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum SearchOperation {
  AND_PREDICATE("and") {
    @Override
    public Predicate build(Path<?> path, Object value, CriteriaBuilder cb) {
      throw new UnsupportedOperationException(
          "Logical AND is handled via specification composition, not individual path predicates.");
    }
  },
  OR_PREDICATE("or") {
    @Override
    public Predicate build(Path<?> path, Object value, CriteriaBuilder cb) {
      throw new UnsupportedOperationException(
          "Logical OR is handled via specification composition, not individual path predicates.");
    }
  },
  GREATER_THAN("gt") {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Predicate build(Path<?> path, Object value, CriteriaBuilder cb) {
      return cb.greaterThan((Expression) path, (Comparable) value);
    }
  },
  LESS_THAN("lt") {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Predicate build(Path<?> path, Object value, CriteriaBuilder cb) {
      return cb.lessThan((Expression) path, (Comparable) value);
    }
  },
  GREATER_THAN_EQUAL("ge") {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Predicate build(Path<?> path, Object value, CriteriaBuilder cb) {
      return cb.greaterThanOrEqualTo((Expression) path, (Comparable) value);
    }
  },
  LESS_THAN_EQUAL("le") {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Predicate build(Path<?> path, Object value, CriteriaBuilder cb) {
      return cb.lessThanOrEqualTo((Expression) path, (Comparable) value);
    }
  },
  NOT_EQUAL("neq") {
    @Override
    public Predicate build(Path<?> path, Object value, CriteriaBuilder cb) {
      return cb.notEqual(path, value);
    }
  },
  EQUAL("eq") {
    @Override
    public Predicate build(Path<?> path, Object value, CriteriaBuilder cb) {
      return cb.equal(path, value);
    }
  },
  LIKE("like") {
    @Override
    public Predicate build(Path<?> path, Object value, CriteriaBuilder cb) {
      return cb.like(cb.lower(path.as(String.class)), "%" + value.toString().toLowerCase() + "%");
    }
  },
  LIKE_START("startlike") {
    @Override
    public Predicate build(Path<?> path, Object value, CriteriaBuilder cb) {
      return cb.like(cb.lower(path.as(String.class)), value.toString().toLowerCase() + "%");
    }
  },
  LIKE_END("endlike") {
    @Override
    public Predicate build(Path<?> path, Object value, CriteriaBuilder cb) {
      return cb.like(cb.lower(path.as(String.class)), "%" + value.toString().toLowerCase());
    }
  },
  IN("in") {
    @Override
    public Predicate build(Path<?> path, Object value, CriteriaBuilder cb) {
      return path.in(value);
    }
  },
  NOT_IN("not") {
    @Override
    public Predicate build(Path<?> path, Object value, CriteriaBuilder cb) {
      return cb.not(path.in(value));
    }
  },
  BETWEEN("between") {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Predicate build(Path<?> path, Object value, CriteriaBuilder cb) {
      if (value instanceof List<?> list && list.size() >= 2) {
        return cb.between((Expression) path, (Comparable) list.get(0), (Comparable) list.get(1));
      }
      throw new IllegalArgumentException(
          "BETWEEN operation requires a List payload containing at least 2 elements.");
    }
  };

  private final String operationName;

  SearchOperation(String operationName) {
    this.operationName = operationName;
  }

  public String getName() {
    return operationName;
  }

  public static final List<SearchOperation> PREDICATES = Arrays.asList(AND_PREDICATE, OR_PREDICATE);

  public static final List<SearchOperation> SEARCH_OPERATIONS =
      Arrays.asList(
          GREATER_THAN,
          LESS_THAN,
          GREATER_THAN_EQUAL,
          LESS_THAN_EQUAL,
          NOT_EQUAL,
          EQUAL,
          LIKE,
          LIKE_START,
          LIKE_END,
          IN,
          NOT_IN,
          BETWEEN);

  public static SearchOperation getSearchOperationByName(final String operationName) {
    Optional<SearchOperation> searchOperationOptional =
        Arrays.stream(SearchOperation.values())
            .filter(operation -> operation.getName().equalsIgnoreCase(operationName))
            .findFirst();
    return searchOperationOptional.orElseThrow(
        () ->
            new SearchOperationNotFoundException(
                String.format("Search operation '%s' is not found.", operationName)));
  }

  public static boolean isSearchOperation(final String operation) {
    return SEARCH_OPERATIONS.stream()
        .anyMatch(searchOperation -> searchOperation.getName().equalsIgnoreCase(operation));
  }

  public static boolean isPredicate(final String operation) {
    return PREDICATES.stream()
        .anyMatch(searchOperation -> searchOperation.getName().equalsIgnoreCase(operation));
  }

  public abstract Predicate build(Path<?> path, Object value, CriteriaBuilder cb);
}
