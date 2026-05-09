package com.mjc.school.service.validator.checker;

import com.mjc.school.service.validator.constraint.Size;
import org.springframework.stereotype.Component;

@Component
public class SizeConstraintChecker implements ConstraintChecker<Size> {

    @Override
    public boolean check(final Object value, final Size constraint) {
        if (value instanceof CharSequence string) {
            return (constraint.min() < 0 || constraint.min() <= string.length())
                && (constraint.max() < 0 || constraint.max() >= string.length());
        }
        return true;
    }

    @Override
    public Class<Size> getType() {
        return Size.class;
    }
}
