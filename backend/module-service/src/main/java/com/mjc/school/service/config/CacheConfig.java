package com.mjc.school.service.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching(proxyTargetClass = true)
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(){
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(defaultSpec());

        manager.registerCustomCache("news",buildCache(10, 500));
        manager.registerCustomCache("newsPage",buildCache(5, 200));
        manager.registerCustomCache("authors",buildCache(30, 300));
        manager.registerCustomCache("tags",buildCache(30, 300));
        manager.registerCustomCache("comments",buildCache(15, 500));

        return manager;
    }

    private Caffeine<Object,Object> defaultSpec(){
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(100)
                .recordStats();
    }

    private Cache<Object, Object> buildCache(int ttl, long maxSize){
        return Caffeine.newBuilder()
                .expireAfterWrite(ttl, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .recordStats()
                .build();
    }
}
