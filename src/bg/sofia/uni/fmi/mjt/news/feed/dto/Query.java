package bg.sofia.uni.fmi.mjt.news.feed.dto;

import java.util.List;

public record Query(int totalResults, List<Article> articles) {
}
