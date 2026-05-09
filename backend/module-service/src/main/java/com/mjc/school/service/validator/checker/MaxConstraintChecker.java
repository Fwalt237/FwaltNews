package com.mjc.school.service.validator.checker;

import com.mjc.school.service.validator.constraint.Max;
import org.springframework.stereotype.Component;

@Component
public class MaxConstraintChecker implements ConstraintChecker<Max> {

    @Override
    public boolean check(final Object value, final Max constraint) {
        return !(value instanceof Number number) || number.longValue() <= constraint.value();
    }

    @Override
    public Class<Max> getType() {
        return Max.class;
    }
}
