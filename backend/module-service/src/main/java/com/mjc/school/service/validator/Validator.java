package com.mjc.school.service.validator;

import java.util.Set;

public interface Validator {

    Set<ConstraintViolation> validate(Object object);
}
