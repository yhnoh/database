package org.example.springredis.example01;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;


/**
 * 회원별 실시간 검색 기능 제작
 * 1. 검섹어를 입력하면 SortedSet의 스코어를 1씩 증가시킨다.
 * 2. 해당 검색어를 기반으로 정렬하여 검색어를 입력한다.
 * 3. 한시간 단위로 데이터를 입력한다.
 * <p>
 * 2. 시간 단위로 검색어를 입력한다.
 * 3.
 */
@Configuration
@RequiredArgsConstructor
public class RedisExampleConfig01 {

    private final StringRedisTemplate stringRedisTemplate;

    @Bean
    public RedisExampleService01 redisExampleService() {
        return new RedisExampleService01(stringRedisTemplate);
    }

}
