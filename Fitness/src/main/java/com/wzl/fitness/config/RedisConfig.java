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
import java.util.HashMap;
import java.util.Map;

/**
 * Redis缓存配置类
 * 配置Redis缓存管理器和序列化方式
 * 仅在Redis可用时启用
 * 
 * 注意：Caffeine 缓存管理器是主缓存管理器（@Primary）
 * Redis 缓存管理器作为可选的分布式缓存方案
 * 
 * 缓存策略优化:
 * - 不同数据类型使用不同TTL
 * - 热点数据使用较长TTL
 * - 频繁变化数据使用较短TTL
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
     * 使用分层缓存策略，不同缓存区域使用不同TTL
     * 
     * 注意：此缓存管理器不是主缓存管理器，Caffeine 是主缓存管理器
     * 如需使用 Redis 缓存，请在 @Cacheable 注解中指定 cacheManager = "redisCacheManager"
     */
    @Bean(name = "redisCacheManager")
    public RedisCacheManager redisCacheManager(RedisConnectionFactory factory) {
        // 默认缓存配置 - 10分钟TTL
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();
        
        // 不同缓存区域的配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 用户信息缓存 - 30分钟TTL（用户信息变化较少）
        cacheConfigurations.put("userCache", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 用户资料缓存 - 15分钟TTL
        cacheConfigurations.put("userProfileCache", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // 训练记录缓存 - 5分钟TTL（数据变化较频繁）
        cacheConfigurations.put("trainingRecordCache", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 营养记录缓存 - 5分钟TTL
        cacheConfigurations.put("nutritionRecordCache", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 仪表盘统计缓存 - 2分钟TTL（需要较新数据）
        cacheConfigurations.put("dashboardStatsCache", defaultConfig.entryTtl(Duration.ofMinutes(2)));
        
        // 训练建议缓存 - 1小时TTL（AI建议不需要频繁更新）
        cacheConfigurations.put("trainingSuggestionCache", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 食物数据库缓存 - 24小时TTL（静态数据）
        cacheConfigurations.put("foodDatabaseCache", defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // 创建缓存管理器
        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .enableStatistics()
                .build();
    }
}