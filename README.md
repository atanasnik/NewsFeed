# NewsFeed :newspaper:
A small project focused on practicing HTTP queries, Design patterns and JSON, given as a homework in the Modern Java Technologies course, FMI, 2023

## Task overview

Create a project which lets the User search for news by different criteria

### API

We are going to use the following [REST API](https://newsapi.org/), it is public, free and works throgh the use of an API Key which can be obtained upon [registration](https://newsapi.org/register).

The necessary endpoint is `/v2/top-headlines`, documentet [here](https://newsapi.org/docs/endpoints/top-headlines).

The API has a limit of **100 requests per day**, which implies its resources should be used carefully upon development and testing.

### Criteria

The criteria which the searches will be based on:
- keywords 
- category (optional)
- country (optional)

### Paging

The User should be able to read up to 3 pages, each one of them containing up to 50 news articles.
