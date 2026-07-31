package com.itsean.campus_second_hand.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        log.info("开始创建redis模板对象...");

        // 创建 ObjectMapper 并注册 JavaTimeModule
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Key 序列化
        template.setKeySerializer(new StringRedisSerializer());
        // Value 序列化
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        // Hash Key
        template.setHashKeySerializer(new StringRedisSerializer());
        // Hash Value
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

        template.afterPropertiesSet();
        return template;
    }
}
