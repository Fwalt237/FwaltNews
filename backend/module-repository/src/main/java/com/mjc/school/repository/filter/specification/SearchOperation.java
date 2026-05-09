package com.mjc.school.repository.filter.specification;

import com.mjc.school.repository.exception.SearchOperationNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum SearchOperation {
    AND_PREDICATE("and"),
    OR_PREDICATE("or"),
    GREATER_THAN("gt"),
    LESS_THAN("lt"),
    GREATER_THAN_EQUAL("ge"),
    LESS_THAN_EQUAL("le"),
    NOT_EQUAL("neq"),
    EQUAL("eq"),
    LIKE("like"),
    LIKE_START("startlike"),
    LIKE_END("endlike"),
    IN("in"),
    NOT_IN("not"),
    BETWEEN("between");

    private String operationName;

    SearchOperation (String operationName) {
        this.operationName = operationName;
    }

   public String getName() {
      return operationName;
   }

    public final static List<SearchOperation> PREDICATES = Arrays.asList(AND_PREDICATE, OR_PREDICATE);

    public final static List<SearchOperation> SEARCH_OPERATIONS = Arrays.asList(GREATER_THAN,
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

    public final static List<SearchOperation> LIKE_CRITERIA_OPERATIONS = Arrays.asList(LIKE,
            LIKE_START,
            LIKE_END);

    public static SearchOperation getSearchOperationByName(final String operationName) {
        Optional<SearchOperation> searchOperationOptional = Arrays.stream(SearchOperation.values()).filter(operation -> operation.getName().equalsIgnoreCase(operationName)).findFirst();
        return searchOperationOptional.orElseThrow(() -> new SearchOperationNotFoundException(String.format("Search operation '%s' is not found.", operationName)));
    }

    public static boolean isSearchOperation(final String operation) {
        return SEARCH_OPERATIONS.stream().anyMatch(searchOperation -> searchOperation.getName().equalsIgnoreCase(operation));
    }

    public static boolean isPredicate(final String operation) {
        return PREDICATES.stream().anyMatch(searchOperation -> searchOperation.getName().equalsIgnoreCase(operation));
    }
}


