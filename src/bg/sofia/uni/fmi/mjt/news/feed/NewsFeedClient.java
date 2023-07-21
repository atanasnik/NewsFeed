package bg.sofia.uni.fmi.mjt.news.feed;

import bg.sofia.uni.fmi.mjt.news.feed.dto.Article;
import bg.sofia.uni.fmi.mjt.news.feed.dto.Query;
import bg.sofia.uni.fmi.mjt.news.feed.exceptions.NewsFeedClientException;
import bg.sofia.uni.fmi.mjt.news.feed.exceptions.BadRequestException;
import bg.sofia.uni.fmi.mjt.news.feed.exceptions.UnauthorizedException;
import bg.sofia.uni.fmi.mjt.news.feed.search.Search;
import com.google.gson.Gson;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;

public class NewsFeedClient {
    private static final String API_KEY = "Paste your API Key here";
    private static final String API_ENDPOINT_SCHEME = "http";
    private static final String API_ENDPOINT_HOST = "newsapi.org";
    private static final String API_ENDPOINT_PATH = "/v2/top-headlines";
    private static final String API_ENDPOINT_FILTER_PAGE = "page=";
    private static final String API_ENDPOINT_API_KEY_PREFIX = "apiKey=";
    private static final String API_ENDPOINT_FILTER_SEPARATOR = "&";
    private static final Gson GSON = new Gson();

    //we are getting 5 articles per query
    private static final int FIXED_MAX_PAGE_SIZE = 100;
    private final HttpClient newsFeedHttpClient;
    private final String apiKey;
    public NewsFeedClient(HttpClient newsFeedHttpClient, String apiKey) {
        this.newsFeedHttpClient = newsFeedHttpClient;
        this.apiKey = apiKey;
    }

    public NewsFeedClient(HttpClient newsFeedHttpClient) {
        this(newsFeedHttpClient, API_KEY);
    }

    private String formatPageNumber(int pageNumber) {
        return API_ENDPOINT_FILTER_PAGE + pageNumber + API_ENDPOINT_FILTER_SEPARATOR;
    }

    public Query getPage(String searchPreferences, int pageNumber) throws NewsFeedClientException {
        HttpResponse<String> response;

        try {
            URI uri = new URI(API_ENDPOINT_SCHEME, API_ENDPOINT_HOST, API_ENDPOINT_PATH,
                    searchPreferences + formatPageNumber(pageNumber) +
                            API_ENDPOINT_API_KEY_PREFIX + apiKey, null);

            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

            response = newsFeedHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new NewsFeedClientException("Could not retrieve news");
        }

        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return GSON.fromJson(response.body(), Query.class);
        } else if (response.statusCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
            throw new BadRequestException("The request is unacceptable, " +
                    "parameters might be missing or placed incorrectly");
        } else if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new UnauthorizedException("API Key missing/incorrect");
        }

        throw new NewsFeedClientException("Unexpected response code from news feed service");
    }

    public List<List<Article>> getFirstPagesOfSearch(Search search) throws NewsFeedClientException {
        if (search.getKeywords().length == 0) {
            throw new NewsFeedClientException("At least one keyword is required");
        }

        String searchPreferences = search.getSearchEndpointString();
        List<Query> pages = new ArrayList<>();
        for (int i = 1; i <= search.getPagesCount(); ++i) {
            pages.add(getPage(searchPreferences, i));
            if (pages.get(i - 1).articles().size() < search.getPageSize()) {
                break;
            }
        }

        return queriesToListsOfArticles(pages);
    }

    public List<List<Article>> getAllPagesOfSearch(Search search) throws NewsFeedClientException {
        if (search.getKeywords().length == 0) {
            throw new NewsFeedClientException("At least one keyword is required");
        }

        String searchPreferences = search.getSearchEndpointString();
        List<Query> pages = new ArrayList<>();

        int validPagesCount = 0;
        do {
            pages.add(getPage(searchPreferences, validPagesCount + 1));
        } while (pages.get(validPagesCount++).articles().size() == search.getPageSize());

        return queriesToListsOfArticles(pages);
    }

    //every list represents a page
    private List<List<Article>> queriesToListsOfArticles(List<Query> list) {
        return list
                .stream()
                .map(Query::articles)
                .toList();
    }

    public List<Article> getSinglePageByNumber(Search search, int n) throws NewsFeedClientException {
        if (n <= 0) {
            throw new IllegalArgumentException("There must be a positive number of pages to be viewed");
        }

        Query page = getPage(search.getSearchEndpointString(), n);

        if (page.totalResults() < n) {
            return new ArrayList<Article>();
        } else {
            return page.articles();
        }
    }
}
