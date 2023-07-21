package bg.sofia.uni.fmi.mjt.news.feed;

import bg.sofia.uni.fmi.mjt.news.feed.dto.Article;
import bg.sofia.uni.fmi.mjt.news.feed.dto.ArticleSource;
import bg.sofia.uni.fmi.mjt.news.feed.dto.Query;
import bg.sofia.uni.fmi.mjt.news.feed.exceptions.NewsFeedClientException;
import bg.sofia.uni.fmi.mjt.news.feed.exceptions.BadRequestException;
import bg.sofia.uni.fmi.mjt.news.feed.exceptions.UnauthorizedException;
import bg.sofia.uni.fmi.mjt.news.feed.search.Search;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NewsFeedClientTest {

    private static final int TEST_ARTICLES_COUNT = 237;
    private static final int FIXED_PAGE_SIZE = 100;
    private static String newsListJsonPage1;
    private static String newsListJsonPage2;
    private static String newsListJsonPage3;
    private static List<Query> pages;
    @Mock
    private HttpClient newsFeedHttpClientMock;
    @Mock
    private HttpResponse<String> newsFeedHttpResponseMock;
    private NewsFeedClient client;

    @BeforeAll
    public static void setUpClass() {
        List<ArticleSource> sources = new ArrayList<>();
        for (int i = 1; i <= TEST_ARTICLES_COUNT; ++i) {
            sources.add(new ArticleSource("id" + i, "source" + i));
        }

        pages = new ArrayList<>();
        int currentPage = -1;
        for (int i = 1; i <= TEST_ARTICLES_COUNT; ++i) {

            Article current = new Article(sources.get(i - 1), "author" + i, "title" + i,
                    "description" + i, "url" + i,
                    "urlToImage" + i, "publishedAt" + i, "content" + i);

            if ((i - 1) % FIXED_PAGE_SIZE == 0) {
                ++currentPage;
                pages.add(new Query(TEST_ARTICLES_COUNT, new ArrayList<>()));
            }
            pages.get(currentPage).articles().add(current);
        }

        newsListJsonPage1 = new Gson().toJson(pages.get(0));
        newsListJsonPage2 = new Gson().toJson(pages.get(1));
        newsListJsonPage3 = new Gson().toJson(pages.get(2));

    }

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(newsFeedHttpClientMock.send(Mockito.any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(newsFeedHttpResponseMock);

        client = new NewsFeedClient(newsFeedHttpClientMock);
    }

    @Test
    public void testGetAllNewsValidKeyword() throws NewsFeedClientException {
        when(newsFeedHttpResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(newsFeedHttpResponseMock.body())
                .thenReturn(newsListJsonPage1, newsListJsonPage2, newsListJsonPage3);

        Search search = Search.builder("biden").build();
        var result = client.getAllPagesOfSearch(search);

        assertEquals(result, pages.stream().map(Query::articles).toList(),
                "NewsFeed with a valid keyword must be retrieved correctly");
    }

    @Test
    public void testGetFirstThreePages() throws NewsFeedClientException {
        when(newsFeedHttpResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(newsFeedHttpResponseMock.body())
                .thenReturn(newsListJsonPage1, newsListJsonPage2, newsListJsonPage3);

        Search search = Search
                .builder("biden")
                .setPageSize(100)
                .setCategory("politics")
                .setCountry("us")
                .setPagesCount(3)
                .build();
        var pagesList = pages.stream().map(Query::articles).toList();
        var result = client.getFirstPagesOfSearch(search);

        assertEquals(pagesList, result, "NewsFeed with a valid keyword must be retrieved correctly");
    }

    @Test
    public void testGetPagesInvalidParameters() {
        when(newsFeedHttpResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);

        //the case when Search's getSearchEndpointString() returns an invalid result
        assertThrows(BadRequestException.class,
                () -> client.getFirstPagesOfSearch(Search.builder("a").setCountry("wakanda").build()),
                "Incorrect parameters case must be covered with an exception");

    }

    @Test
    public void testGetPagesInvalidAPIKey() {
        when(newsFeedHttpResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_UNAUTHORIZED);

        assertThrows(UnauthorizedException.class,
                () -> client.getFirstPagesOfSearch(Search.builder("cmon").setCountry("narnia").build()),
                "Invalid API key case must be covered with an exception");

    }

    @Test
    public void testGetSinglePageByNumberValid() throws NewsFeedClientException {
        when(newsFeedHttpResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(newsFeedHttpResponseMock.body()).thenReturn(newsListJsonPage3);

        Search search = Search.builder("ew", "no").setCountry("uk").setCategory("economy").build();
        var page = pages.get(3 - 1).articles();
        var result = client.getSinglePageByNumber(search, 3);

        assertEquals(page, result, "A single page must be retrieved correctly");
    }
}
