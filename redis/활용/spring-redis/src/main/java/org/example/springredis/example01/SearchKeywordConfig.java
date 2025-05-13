package org.example.springredis.example01;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * <p>키워드 검색량에 따른 인기 검색어 구현<p/>
 * <ol>
 *     <li>검섹어를 입력하면 키워드 검색량이 1씩 증가한다. 키워드 검색량 데이터는 시간당 기준으로 저장된다.</li>
 *     <li>해당 키워드 검색량을 기준으로 랭킹을 매겨서 보여준다.</li>
 *     <li>이전 키워드 검색량과의 랭킹 차이를 보여준다.</li>
 * <ol/>
 */
@Configuration
@RequiredArgsConstructor
public class SearchKeywordConfig {

    private final StringRedisTemplate stringRedisTemplate;
    public static final LocalDateTime NOW_DATETIME = LocalDateTime.of(2025, 1, 1, 20, 30);
    public static final LocalDateTime BEFORE_DATETIME = NOW_DATETIME.minusHours(1);

    @PostConstruct
    public void postConstruct() {
        // 기존 테스트 데이터 삭제
        String nowKey = new RedisSearchKeywordKey(NOW_DATETIME).getKey();
        String beforeKey = new RedisSearchKeywordKey(BEFORE_DATETIME).getKey();
        stringRedisTemplate.delete(List.of(nowKey, beforeKey));

        this.setRandomData(nowKey, 20);
        this.setRandomData(beforeKey, 20);
    }

    /**
     * 실시간 검색어 구현을 위한 테스트 데이터 셋팅
     */
    public void setRandomData(String key, int count) {
        BoundZSetOperations<String, String> sortedSet = stringRedisTemplate.boundZSetOps(key);
        Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
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
    public SearchKeywordService redisExampleService() {
        return new SearchKeywordService(stringRedisTemplate);
    }

}
