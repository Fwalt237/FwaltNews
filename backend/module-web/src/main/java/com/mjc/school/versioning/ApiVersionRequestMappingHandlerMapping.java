package com.mjc.school.versioning;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

public class ApiVersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
    @Override
    @Nullable
    protected RequestCondition<ApiVersionCondition> getCustomTypeCondition(@NotNull Class<?> handlerType) {
        return createCondition(AnnotationUtils.findAnnotation(handlerType, ApiVersion.class));
    }

    @Override
    @Nullable
    protected RequestCondition<ApiVersionCondition> getCustomMethodCondition(@NotNull Method method) {
        return createCondition(AnnotationUtils.findAnnotation(method, ApiVersion.class));
    }

    @Nullable
    private RequestCondition<ApiVersionCondition> createCondition(ApiVersion apiVersion) {
        return apiVersion == null ? null : new ApiVersionCondition(apiVersion.value());
    }
}
