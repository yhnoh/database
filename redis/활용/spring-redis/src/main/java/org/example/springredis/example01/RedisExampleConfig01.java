package org.example.springredis.example01;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;


/**
 * 회원별 실시간 검색 기능 제작
 * 1. 검섹어를 입력하면 카운팅이 올라간다.
 * 2. 시간 단위로 검색어를 입력한다.
 * 3.
 */
@Configuration
@RequiredArgsConstructor
public class RedisExampleConfig01 {

    private final StringRedisTemplate stringRedisTemplate;

    @Bean
    public RedisExampleService01 test() {
        return new RedisExampleService01(stringRedisTemplate);
    }
}
