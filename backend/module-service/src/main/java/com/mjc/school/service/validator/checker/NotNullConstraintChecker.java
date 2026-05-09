package com.mjc.school.service.validator.checker;

import com.mjc.school.service.validator.constraint.NotNull;
import org.springframework.stereotype.Component;

@Component
public class NotNullConstraintChecker implements ConstraintChecker<NotNull> {

    @Override
    public boolean check(final Object value, final NotNull constraint) {
        return value != null;
    }

    @Override
    public Class<NotNull> getType() {
        return NotNull.class;
    }
}
