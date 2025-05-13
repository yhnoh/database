package org.example.springredis.example01;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class RedisSearchKeywordKey {

    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter LOCAL_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH");

    private final LocalDateTime searchedDateTime;

    public RedisSearchKeywordKey(LocalDateTime searchedDateTime) {
        this.searchedDateTime = searchedDateTime;
    }

    public String getKey() {
        LocalDate localDate = searchedDateTime.toLocalDate();
        LocalTime localTime = searchedDateTime.toLocalTime();
        return "search:keyword:" + localDate.format(LOCAL_DATE_FORMATTER) + ":" + localTime.format(LOCAL_TIME_FORMATTER);
    }
}
