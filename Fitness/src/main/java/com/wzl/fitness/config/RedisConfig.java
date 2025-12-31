package com.wzl.fitness.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis缓存配置类
 * 配置Redis缓存管理器和序列化方式
 * 仅在Redis可用时启用
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisConfig {

    /**
     * 配置RedisTemplate，用于操作Redis
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        
        // 设置键的序列化器
        template.setKeySerializer(new StringRedisSerializer());
        // 设置哈希键的序列化器
        template.setHashKeySerializer(new StringRedisSerializer());
        // 设置值的序列化器
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // 设置哈希值的序列化器
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置Redis缓存管理器
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        // 设置缓存序列化方式
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                // 设置默认的过期时间为10分钟
                .entryTtl(Duration.ofMinutes(10))
                // 设置键的序列化器
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 设置值的序列化器
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                // 禁用缓存空值
                .disableCachingNullValues();
        
        // 创建缓存管理器
        return RedisCacheManager.builder(factory)
                .cacheDefaults(cacheConfig)
                // 允许动态创建缓存
                .enableStatistics()
                .build();
    }
}