package com.mjc.school.repository.filter.sorting;

import java.util.Arrays;

public enum SortOrder {
    ASC,
    DESC;

    public static boolean isSortOrderExisted(final String sortOrderName) {
        return Arrays.stream(values()).anyMatch(sortOrder -> sortOrder.name().equalsIgnoreCase(sortOrderName));
    }
}
