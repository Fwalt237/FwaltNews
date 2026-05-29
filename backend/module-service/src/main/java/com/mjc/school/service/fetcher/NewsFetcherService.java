package com.mjc.school.service.fetcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjc.school.repository.impl.NewsRepository;
import com.mjc.school.service.config.properties.NewsDataApiProperties;
import com.mjc.school.service.dto.NewsDataItem;
import com.mjc.school.service.dto.NewsDataResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class NewsFetcherService {

  private static final Logger log = LoggerFactory.getLogger(NewsFetcherService.class);

  private final NewsDataApiProperties newsDataApiProperties;
  private final NewsDataApiProperties apiKeyConfig;
  private final NewsRepository newsRepository;
  private final NewsPersistence newsPersistence;
  private final ObjectMapper mapper;
  private final HttpClient httpClient;
  private final ArticleScraper scraper;

  @Autowired
  public NewsFetcherService(
      NewsRepository newsRepository,
      NewsPersistence newsPersistence,
      NewsDataApiProperties apiKeyConfig,
      ObjectMapper mapper,
      ArticleScraper scraper,
      NewsDataApiProperties newsDataApiProperties) {
    this.newsRepository = newsRepository;
    this.newsPersistence = newsPersistence;
    this.apiKeyConfig = apiKeyConfig;
    this.mapper = mapper;
    this.httpClient = HttpClient.newHttpClient();
    this.scraper = scraper;
    this.newsDataApiProperties = newsDataApiProperties;
  }

  @Scheduled(cron = "${news.scheduler.fetch-cron}")
  @SchedulerLock(name = "NewsFetcher_fetchLatestNews", lockAtMostFor = "15m", lockAtLeastFor = "5m")
  public void fetchLatestNews() {
    try {
      List<NewsDataItem> items = callApi();
      if (items == null || items.isEmpty()) return;

      int saved = 0;
      for (NewsDataItem item : items) {
        if (item.title() == null || newsRepository.existsByTitle(item.title())) {
          continue;
        }

        try {
          String scrapedBody = scraper.scrape(item.link());
          newsPersistence.persist(item, scrapedBody);
          saved++;
        } catch (Exception e) {
          log.error("Couldn't save article '{}':{}", item.title(), e.getMessage());
        }
      }
      log.info("Fetched and saved {} new articles", saved);
    } catch (InterruptedException e) {
      log.error("News fetch interrupted: {}", e.getMessage());
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      log.error("News fetch failed: {}", e.getMessage());
    }
  }

  @Scheduled(cron = "${news.scheduler.purge-old}")
  @SchedulerLock(name = "NewsFetcher_purgeOldNews", lockAtMostFor = "5m", lockAtLeastFor = "1m")
  @Transactional
  @CacheEvict(
      value = {"news", "newsPage"},
      allEntries = true)
  public void purgeOldNews() {
    LocalDateTime date = LocalDateTime.now().minusDays(30);
    newsRepository.deleteOlderThan(date);
    log.info("Purged news articles older than 30 days.");
  }

  private List<NewsDataItem> callApi() throws IOException, InterruptedException {
    String url =
        UriComponentsBuilder.fromUriString(newsDataApiProperties.getSourceUrl())
            .queryParam("apikey", apiKeyConfig.getKey())
            .queryParam("language", "en")
            .queryParam("size", 10)
            .toUriString();

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .header("User-Agent", "Java/11")
            .GET()
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      if (log.isErrorEnabled()) {
        log.error("An error happened. Status: {} Body: {}", response.statusCode(), response.body());
      }
      return List.of();
    }

    NewsDataResponse news = mapper.readValue(response.body(), NewsDataResponse.class);
    return (news.results() != null) ? news.results() : List.of();
  }
}
