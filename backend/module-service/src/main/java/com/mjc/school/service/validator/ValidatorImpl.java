package com.mjc.school.service.validator;

import com.mjc.school.service.validator.checker.ConstraintChecker;
import com.mjc.school.service.validator.constraint.Constraint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static java.util.stream.Collectors.toMap;

@Component
@SuppressWarnings({"unchecked", "rawtypes"})
public class ValidatorImpl implements Validator {

    private static final Logger logger = LoggerFactory.getLogger(ValidatorImpl.class);

    private final Map<Class<? extends Annotation>, ConstraintChecker> checkersMap;

    @Autowired
    public ValidatorImpl(final List<ConstraintChecker> checkers) {
        this.checkersMap = checkers
            .stream()
            .collect(toMap(ConstraintChecker::getType, Function.identity()));
    }

    public Set<ConstraintViolation> validate(final Object object) {
        if (object == null) {
            return Collections.emptySet();
        }
        var violations = new HashSet<ConstraintViolation>();
        for (var declaredField : object.getClass().getDeclaredFields()) {
            validateField(violations, declaredField, object);
        }
        return violations;
    }

    private void validateObject(Set<ConstraintViolation> violations, final Object object) {
        if (object == null) {
            return;
        }
        for (var declaredField : object.getClass().getDeclaredFields()) {
            validateField(violations, declaredField, object);
        }
    }

    private void validateField(
        final Set<ConstraintViolation> violations,
        final Field field,
        final Object instance
    ) {
        for (var declaredAnnotation : field.getDeclaredAnnotations()) {
            var annotationType = declaredAnnotation.annotationType();
            if (annotationType.isAnnotationPresent(Constraint.class)) {
                try {
                    if (field.trySetAccessible() && field.canAccess(instance)) {
                        var value = field.get(instance);
                        var checker = checkersMap.get(annotationType);
                        if (checker != null && !checker.check(value, annotationType.cast(declaredAnnotation))) {
                            violations.add(
                                new ConstraintViolation(
                                    "Constraint '%s' violated for the value '%s'".formatted(
                                        annotationType.getSimpleName(),
                                        value
                                    )
                                )
                            );
                        }
                        validateObject(violations, value);
                    }
                } catch (IllegalAccessException e) {
                    logger.warn("Unable to access field '{}' for validation: {}",
                            field.getName(), e.getMessage());
                }
            }
        }
    }
}


