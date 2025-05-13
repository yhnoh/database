package org.example.springredis.example01;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchKeyword {
    private String value;
    private Double score;
    private int rank;
}
