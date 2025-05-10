package org.example.springredis.example01;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/example01")
@RequiredArgsConstructor
public class RedisExampleController01 {

    private final RedisExampleService01 redisExampleConfig01;

    @GetMapping("/search")
    public double search(@RequestParam String keyword) {
        SearchCommand command = SearchCommand.builder()
                .keyword(keyword)
                .searchedDataTime(ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()))
                .build();

        return redisExampleConfig01.getSearch(command);
    }

    @GetMapping("/search/popular-keyword")
    public List<SearchPopularKeyword> getSearchPopularKeyword() {

        SearchPopularKeywordCommand command = SearchPopularKeywordCommand.builder()
                .top(10)
                .searchedDataTime(ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()))
                .build();

        return redisExampleConfig01.getSearchPopularKeywords(command);
    }

}
