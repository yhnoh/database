package org.example.springredis.example01;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class RedisExampleService01 {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 검색을 할때마다 검색어의 스코어를 1씩 증가시킨다.
     */
    public double getSearch(SearchCommand command) {
        BoundZSetOperations<String, String> sortedSet = stringRedisTemplate.boundZSetOps(command.getKey());
        return sortedSet.incrementScore(command.getKeyword(), 1);
    }

    // 스코어 기준으로 상위 10개의 데이터를 가져온다.
    public List<SearchPopularKeyword> getSearchPopularKeywords(SearchPopularKeywordCommand command) {
        BoundZSetOperations<String, String> sortedSet = stringRedisTemplate.boundZSetOps(command.getKey());
        Set<ZSetOperations.TypedTuple<String>> typedTuples = sortedSet.reverseRangeWithScores(0, -1);

        List<SearchPopularKeyword> searchPopularKeywords = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            SearchPopularKeyword searchPopularKeyword = SearchPopularKeyword.builder()
                    .keyword(typedTuple.getValue())
                    .count(typedTuple.getScore())
                    .rank(rank)
                    .build();

            searchPopularKeywords.add(searchPopularKeyword);
            rank++;
        }

        return searchPopularKeywords;
    }

}
