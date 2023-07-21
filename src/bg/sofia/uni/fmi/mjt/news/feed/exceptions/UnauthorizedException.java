package bg.sofia.uni.fmi.mjt.news.feed.exceptions;

public class UnauthorizedException extends NewsFeedClientException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
