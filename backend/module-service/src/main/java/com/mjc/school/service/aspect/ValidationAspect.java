package com.mjc.school.service.aspect;

import com.mjc.school.service.exceptions.ServiceErrorCode;
import com.mjc.school.service.exceptions.ValidatorException;
import com.mjc.school.service.validator.ConstraintViolation;
import com.mjc.school.service.validator.Valid;
import com.mjc.school.service.validator.Validator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.stream.Stream;

@Aspect
@Component
public class ValidationAspect {

    private final Validator validator;

    @Autowired
    public ValidationAspect(Validator validator) {
        this.validator = validator;
    }

    @Before("execution(public * *(.., @com.mjc.school.service.validator.Valid (*), ..))")
    public void validateBeforeInvocation(final JoinPoint joinPoint) throws NoSuchMethodException {
        if (joinPoint.getSignature() instanceof MethodSignature signature) {
            var targetMethod = getTargetMethod(joinPoint, signature);
            var args = joinPoint.getArgs();
            var parameterAnnotations = targetMethod.getParameterAnnotations();

            var violations = new HashSet<ConstraintViolation>();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                if (requiresValidation(parameterAnnotations[i])) {
                    violations.addAll(validator.validate(args[i]));
                }
            }

            if (!violations.isEmpty()) {
                throw new ValidatorException(String.format(ServiceErrorCode.VALIDATION.getMessage(), violations));
            }
        }
    }

    private Method getTargetMethod(JoinPoint joinPoint, MethodSignature signature) throws NoSuchMethodException {
        Method baseMethod = signature.getMethod();
        return joinPoint.getTarget().getClass()
            .getMethod(baseMethod.getName(), baseMethod.getParameterTypes());
    }

    private boolean requiresValidation(final Annotation[] annotations) {
        return Stream.of(annotations).anyMatch(Valid.class::isInstance);
    }
}