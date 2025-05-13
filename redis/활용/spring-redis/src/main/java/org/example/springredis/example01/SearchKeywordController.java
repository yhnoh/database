package org.example.springredis.example01;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SearchKeywordController {

    private final SearchKeywordService searchKeywordService;

    @GetMapping("/search-keywords")
    public Map<String, List<SearchKeyword>> getSearchKeywords() {
        return searchKeywordService.getSearchKeywords();
    }

    @PostMapping("/search-keywords")
    public double incrementSearchKeyword(@RequestParam String keyword) {

        return searchKeywordService.incrementSearchKeyword(keyword);
    }


    @GetMapping("/search-keywords/popular")
    public List<SearchPopularKeyword> getPopularSearchKeywords(@RequestParam(required = false, defaultValue = "10") int top) {
        return searchKeywordService.getPopularSearchKeywords(top);
    }


}
