package org.example.springredis.example01;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.*;

import static org.example.springredis.example01.SearchKeywordConfig.BEFORE_DATETIME;
import static org.example.springredis.example01.SearchKeywordConfig.NOW_DATETIME;

@RequiredArgsConstructor
public class SearchKeywordService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 검색된 키워드 전체 리스트
     */
    public Map<String, List<SearchKeyword>> getSearchKeywords() {
        BoundZSetOperations<String, String> nowSortedSet = stringRedisTemplate.boundZSetOps(new RedisSearchKeywordKey(NOW_DATETIME).getKey());
        BoundZSetOperations<String, String> beforeSortedSet = stringRedisTemplate.boundZSetOps(new RedisSearchKeywordKey(BEFORE_DATETIME).getKey());

        Set<ZSetOperations.TypedTuple<String>> nowTuples = nowSortedSet.reverseRangeWithScores(0, -1);

        List<SearchKeyword> nowSearchKeywords = new ArrayList<>();
        int nowRank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : nowTuples) {
            nowSearchKeywords.add(SearchKeyword.builder()
                    .value(tuple.getValue())
                    .score(tuple.getScore())
                    .rank(nowRank)
                    .build());
            nowRank++;
        }

        Set<ZSetOperations.TypedTuple<String>> beforeTuples = beforeSortedSet.reverseRangeWithScores(0, -1);
        List<SearchKeyword> beforeSearchKeywords = new ArrayList<>();
        int beforeRank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : beforeTuples) {
            beforeSearchKeywords.add(SearchKeyword.builder()
                    .value(tuple.getValue())
                    .score(tuple.getScore())
                    .rank(beforeRank)
                    .build());
            beforeRank++;
        }

        Map<String, List<SearchKeyword>> result = new HashMap<>();
        result.put("now", nowSearchKeywords);
        result.put("before", beforeSearchKeywords);

        return result;
    }

    /**
     * 검색을 할때마다 검색어의 스코어를 1씩 증가시킨다.
     */
    public double incrementSearchKeyword(String keyword) {
        BoundZSetOperations<String, String> nowSortedSet = stringRedisTemplate.boundZSetOps(new RedisSearchKeywordKey(NOW_DATETIME).getKey());
        return nowSortedSet.incrementScore(keyword, 1);
    }

    /**
     * 검색량이 많은 키워드를 가져온다.
     */
    public List<SearchPopularKeyword> getPopularSearchKeywords(int top) {
        BoundZSetOperations<String, String> nowSortedSet = stringRedisTemplate.boundZSetOps(new RedisSearchKeywordKey(NOW_DATETIME).getKey());
        BoundZSetOperations<String, String> beforeSortedSet = stringRedisTemplate.boundZSetOps(new RedisSearchKeywordKey(BEFORE_DATETIME).getKey());

        Set<ZSetOperations.TypedTuple<String>> typedTuples = nowSortedSet.reverseRangeWithScores(0, top - 1);


        List<SearchPopularKeyword> searchPopularKeywords = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            String value = typedTuple.getValue();
            Long beforeRank = beforeSortedSet.reverseRank(value);
            beforeRank = beforeRank == null ? 0 : beforeRank + 1;

            SearchPopularKeyword searchPopularKeyword = SearchPopularKeyword.builder()
                    .keyword(value)
                    .count(typedTuple.getScore())
                    .rank(rank)
                    .diff((int) (beforeRank - rank))
                    .build();

            searchPopularKeywords.add(searchPopularKeyword);
            rank++;
        }

        return searchPopularKeywords;
    }


}
