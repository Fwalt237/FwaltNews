package com.mjc.school.service.validator.checker;

import com.mjc.school.repository.filter.specification.SearchOperation;
import com.mjc.school.service.validator.constraint.SearchCriteria;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mjc.school.service.filter.mapper.BaseSearchFilterMapper.SORT_AND_FILTER_DELIMITER;

@Component
public class SearchCriteriaChecker implements ConstraintChecker<SearchCriteria> {
    @Override
    public boolean check(Object value, SearchCriteria constraint) {
        for (String criteria : (List<String>) value) {
            String[] splitCriteria = criteria.split(SORT_AND_FILTER_DELIMITER);
            if (splitCriteria.length < 3) {
                return false;
            }
            String searchOperation = SearchOperation.isPredicate(splitCriteria[0].toUpperCase()) ? splitCriteria[2] : splitCriteria[1];
            if (!SearchOperation.isSearchOperation(searchOperation)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Class<SearchCriteria> getType() {
        return SearchCriteria.class;
    }
}
