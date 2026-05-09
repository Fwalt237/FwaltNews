package com.mjc.school.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.caffeine.CaffeineProxyManager;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public ProxyManager<String> proxyManager(){
        Caffeine<Object, Object> builder =  Caffeine.newBuilder().maximumSize(100);
        return new CaffeineProxyManager<>(builder, Duration.ofMinutes(1));
    }

    @Bean
    public BucketConfiguration bucketConfiguration(){
        return BucketConfiguration.builder()
                .addLimit(limit->limit.capacity(3).refillIntervally(3, Duration.ofMinutes(1)))
                .build();
    }
}
