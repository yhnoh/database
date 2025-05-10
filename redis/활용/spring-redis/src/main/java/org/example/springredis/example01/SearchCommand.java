package org.example.springredis.example01;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
public class SearchCommand {

    private String keyword;
    private ZonedDateTime searchedDataTime;

    public String getKey() {
        return new RedisSearchKeywordKey(searchedDataTime).getKey();
    }
}
