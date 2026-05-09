package com.mjc.school.service.validator.checker;

import com.mjc.school.repository.filter.sorting.SortOrder;
import com.mjc.school.service.validator.constraint.SortAndOrder;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mjc.school.service.filter.mapper.BaseSearchFilterMapper.SORT_AND_FILTER_DELIMITER;

@Component
public class SortAndOrderChecker implements ConstraintChecker<SortAndOrder> {
    @Override
    public boolean check(Object value, SortAndOrder constraint) {
        for (String sort : (List<String>) value) {
            String[] splitSort = sort.split(SORT_AND_FILTER_DELIMITER);
            if (splitSort.length != 2 || !SortOrder.isSortOrderExisted(splitSort[1])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Class<SortAndOrder> getType() {
        return SortAndOrder.class;
    }
}
