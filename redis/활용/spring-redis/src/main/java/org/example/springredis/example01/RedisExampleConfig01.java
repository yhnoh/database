package org.example.springredis.example01;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.TreeSet;


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

    @PostConstruct
    public void postConstruct() {

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime beforeOneHour = now.minusHours(1);
        this.setRandomData(new RedisSearchKeywordKey(now).getKey(), 20);
        this.setRandomData(new RedisSearchKeywordKey(beforeOneHour).getKey(), 20);
    }

    /**
     * 테스트 데이터 셋팅
     */
    public void setRandomData(String key, int count) {
        BoundZSetOperations<String, String> sortedSet = stringRedisTemplate.boundZSetOps(key);
        Set<ZSetOperations.TypedTuple<String>> tuples = new TreeSet<>();
        for (int i = 1; i <= count; i++) {
            //0 ~ 100 사이의 데이터 생성
            String value = "keyword_" + i;
            int score = (int) (Math.random() * 100) + 1;

            ZSetOperations.TypedTuple<String> tuple = ZSetOperations.TypedTuple.of(value, (double) score);
            tuples.add(tuple);
        }

        sortedSet.add(tuples);
    }

    @Bean
    public RedisExampleService01 redisExampleService() {
        return new RedisExampleService01(stringRedisTemplate);
    }

}
