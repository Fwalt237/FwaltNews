package com.mjc.school.aicontroller;

import com.mjc.school.exception.handler.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@WebFilter("/api/v1/ai/chat")
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ProxyManager<String> proxyManager;
    private final BucketConfiguration bucketConfiguration;
    private final HandlerExceptionResolver resolver;

    public RateLimitingFilter(ProxyManager<String> proxyManager,BucketConfiguration bucketConfiguration,
                              @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver){
        this.proxyManager=proxyManager;
        this.bucketConfiguration=bucketConfiguration;
        this.resolver=resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String key = request.getHeader("X-Session-ID");
        if(key==null) key=request.getRemoteAddr();

        Bucket bucket = proxyManager.builder().build(key,()->bucketConfiguration);

        if (bucket.tryConsume(1)){
            filterChain.doFilter(request,response);
        }else{
            resolver.resolveException(request,response,null,
                    new RateLimitExceededException("You have exhausted your 3 messages per minute."));
        }
    }
}
