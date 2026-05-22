package com.financecontrol.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache("transactions",
                Objects.requireNonNull(Caffeine.newBuilder().maximumSize(500).expireAfterWrite(30, TimeUnit.MINUTES).recordStats().build()));
        manager.registerCustomCache("categories",
                Objects.requireNonNull(Caffeine.newBuilder().maximumSize(500).expireAfterWrite(10, TimeUnit.MINUTES).recordStats().build()));
        manager.registerCustomCache("accounts",
                Objects.requireNonNull(Caffeine.newBuilder().maximumSize(500).expireAfterWrite(10, TimeUnit.MINUTES).recordStats().build()));
        manager.registerCustomCache("financialInstitutions",
                Objects.requireNonNull(Caffeine.newBuilder().maximumSize(500).expireAfterWrite(10, TimeUnit.MINUTES).recordStats().build()));
        manager.registerCustomCache("transactionLocales",
                Objects.requireNonNull(Caffeine.newBuilder().maximumSize(500).expireAfterWrite(10, TimeUnit.MINUTES).recordStats().build()));
        return manager;
    }
}
