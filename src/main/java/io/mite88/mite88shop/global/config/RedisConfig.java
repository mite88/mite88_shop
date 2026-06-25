package io.mite88.mite88shop.global.config;

import io.mite88.mite88shop.global.model.AiJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${REDIS_HOST}")
    private String redisHost;

    @Value("${REDIS_PORT}")
    private int redisPort;

    // REDIS_PASSWORD가 없거나 비어있을 경우 빈 문자열("")을 기본값으로 사용
    @Value("${REDIS_PASSWORD:}")
    private String redisPassword;

    /**
     * AiJob 객체 저장용 RedisTemplate - Jackson JSON 직렬화
     * 잘못된 템플릿 사용 시 역직렬화 오류 발생하므로 JobService에서 반드시 이 빈을 사용해야 함
     */
    @Bean
    public RedisTemplate<String, AiJob> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, AiJob> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        JacksonJsonRedisSerializer<AiJob> serializer =
                new JacksonJsonRedisSerializer<>(AiJob.class);

        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * AI 작업 큐(jobId) 관리용 RedisTemplate - String 직렬화
     * AiJob 템플릿과 혼용 금지 (타입 불일치로 직렬화 오류 발생)
     */
    @Bean
    public RedisTemplate<String, String> queueRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis 연결 설정 - 비밀번호 없는 경우 인증 생략
     */
    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isBlank()) {
            config.setPassword(redisPassword);
        }
        return new LettuceConnectionFactory(config);
    }

    /**
     * RefreshToken 등 단순 문자열 저장용 StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(lettuceConnectionFactory);
        template.afterPropertiesSet();
        return template;
    }
}