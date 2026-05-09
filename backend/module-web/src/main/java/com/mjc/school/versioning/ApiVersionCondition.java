package com.mjc.school.versioning;

import com.mjc.school.exception.handler.ApiVersionNotSupportedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.condition.RequestCondition;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiVersionCondition implements RequestCondition<ApiVersionCondition> {

    private final static Pattern VERSION_PREFIX_PATTERN = Pattern.compile("/v(\\d+)/");

    private final int apiVersion;

    public ApiVersionCondition(int apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public ApiVersionCondition combine(ApiVersionCondition otherApiVersionCondition) {
        return new ApiVersionCondition(otherApiVersionCondition.getApiVersion());
    }

    @Override
    public ApiVersionCondition getMatchingCondition(HttpServletRequest request) {
        Matcher m = VERSION_PREFIX_PATTERN.matcher(request.getRequestURI());
        if (m.find()) {
            Integer version = Integer.valueOf(m.group(1));
            if (version == this.apiVersion) {
                return this;
            } else {
                throw new ApiVersionNotSupportedException(String.format("Api version '%s' is not supported.", version));
            }
        }
        throw new ApiVersionNotSupportedException(String.format("Api version in the request uri '%s' is not supported.", request.getRequestURI()));
    }

    @Override
    public int compareTo(ApiVersionCondition otherApiVersionCondition, HttpServletRequest request) {
        return otherApiVersionCondition.getApiVersion() - this.apiVersion;
    }

    private int getApiVersion() {
        return apiVersion;
    }
}
