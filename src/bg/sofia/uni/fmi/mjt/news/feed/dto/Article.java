package bg.sofia.uni.fmi.mjt.news.feed.dto;

public record Article(
        ArticleSource source,
        String author,
        String title,
        String description,
        String url,
        String urlToImage,
        String publishedAt,
        String content) {
}
