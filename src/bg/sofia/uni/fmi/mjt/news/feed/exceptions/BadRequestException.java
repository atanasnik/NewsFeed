package bg.sofia.uni.fmi.mjt.news.feed.exceptions;

public class BadRequestException extends NewsFeedClientException {
    public BadRequestException(String message) {
        super(message);
    }
}
