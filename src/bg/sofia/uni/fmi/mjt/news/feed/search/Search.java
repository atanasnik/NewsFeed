package bg.sofia.uni.fmi.mjt.news.feed.search;

import java.util.stream.Stream;

public class Search {

    private static final String API_ENDPOINT_FILTER_KEYWORDS = "q=";
    private static final String API_ENDPOINT_FILTER_CATEGORY = "category=";
    private static final String API_ENDPOINT_FILTER_COUNTRY = "country=";
    private static final String API_ENDPOINT_FILTER_PAGE_SIZE = "pageSize=";
    private static final String API_ENDPOINT_FILTER_SEPARATOR = "&";
    private static final String API_ENDPOINT_KEYWORD_SEPARATOR = "+";
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGES_NUMBER = 3;

    //the sole required parameter
    private String[] keywords;

    //optional parameters
    private String category;
    private String country;

    //these parameters have default values but can still be updated
    private int pageSize = MAX_PAGE_SIZE;
    private int pagesCount = DEFAULT_PAGES_NUMBER;

    private Search(String... keywords) {
        this.keywords = keywords;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public String getCategory() {
        return category;
    }

    public String getCountry() {
        return country;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPagesCount() {
        return pagesCount;
    }

    private void keywordsToUriString(StringBuffer keywordsQuery, String... keywords) {
        keywordsQuery.append(API_ENDPOINT_FILTER_KEYWORDS);
        Stream.of(keywords)
                .forEach(keyword -> keywordsQuery
                        .append(keyword)
                        .append(API_ENDPOINT_KEYWORD_SEPARATOR));
        keywordsQuery.replace(keywordsQuery.length() - 1, keywordsQuery.length(), API_ENDPOINT_FILTER_SEPARATOR);
    }

    private void categoryToUriString(StringBuffer categoryQuery, String category) {
        if (category != null && !category.isBlank()) {
            categoryQuery
                    .append(API_ENDPOINT_FILTER_CATEGORY)
                    .append(category)
                    .append(API_ENDPOINT_FILTER_SEPARATOR);
        }
    }

    private void countryToUriString(StringBuffer countryQuery, String country) {
        if (country != null && !country.isBlank()) {
            countryQuery
                    .append(API_ENDPOINT_FILTER_COUNTRY)
                    .append(country)
                    .append(API_ENDPOINT_FILTER_SEPARATOR);
        }
    }

    public String getSearchEndpointString() {
        StringBuffer keywordsQuery = new StringBuffer();
        keywordsToUriString(keywordsQuery, getKeywords());

        StringBuffer categoryQuery = new StringBuffer();
        categoryToUriString(categoryQuery, getCategory());

        StringBuffer countryQuery = new StringBuffer();
        countryToUriString(countryQuery, getCountry());

        String pageSizeQuery = API_ENDPOINT_FILTER_PAGE_SIZE + pageSize + API_ENDPOINT_FILTER_SEPARATOR;

        return keywordsQuery.append(categoryQuery).append(countryQuery).append(pageSizeQuery).toString();
    }

    public static SearchBuilder builder(String... keywords) {
        return new SearchBuilder(keywords);
    }

    private Search(SearchBuilder builder) {
        this.keywords = builder.keywords;
        this.category = builder.category;
        this.country = builder.country;
        this.pageSize = builder.pageSize;
        this.pagesCount = builder.pagesCount;
    }

    public static class SearchBuilder {
        private String[] keywords;

        private String category;
        private String country;
        private int pageSize = MAX_PAGE_SIZE;
        private int pagesCount = DEFAULT_PAGES_NUMBER;

        private SearchBuilder(String... keywords) {
            this.keywords = keywords;
        }

        public SearchBuilder setCategory(String category) {
            this.category = category;
            return this;
        }

        public SearchBuilder setCountry(String country) {
            this.country = country;
            return this;
        }

        public SearchBuilder setPageSize(int pageSize) {
            if (pageSize <= 0) {
                throw new IllegalArgumentException("There must be a positive number articles per page");
            }

            this.pageSize = pageSize;
            return this;
        }

        public SearchBuilder setPagesCount(int pagesCount) {
            if (pagesCount <= 0) {
                throw new IllegalArgumentException("There must be a positive number of pages to be viewed");
            }

            this.pagesCount = pagesCount;
            return this;
        }

        public Search build() {
            return new Search(this);
        }
    }
}
