package org.example.springredis.example01;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchPopularKeyword {

    private int rank;
    private String keyword;
    private double count;
}
