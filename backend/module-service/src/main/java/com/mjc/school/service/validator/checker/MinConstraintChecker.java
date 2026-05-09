package com.mjc.school.service.validator.checker;

import com.mjc.school.service.validator.constraint.Min;
import org.springframework.stereotype.Component;

@Component
public class MinConstraintChecker implements ConstraintChecker<Min> {

    @Override
    public boolean check(final Object value, final Min constraint) {
        return !(value instanceof Number number) || number.longValue() >= constraint.value();
    }

    @Override
    public Class<Min> getType() {
        return Min.class;
    }
}
