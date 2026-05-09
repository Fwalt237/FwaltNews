package com.mjc.school.service.aiservice;

import com.mjc.school.repository.airepo.impl.NewsEmbeddingsRepository;
import com.mjc.school.repository.impl.NewsRepository;
import com.mjc.school.repository.model.News;
import com.mjc.school.repository.model.Tag;
import com.mjc.school.service.aiservice.dto.ArticleDetail;
import com.mjc.school.service.aiservice.dto.NewsSearchResult;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class NewsTools {

    private static final Logger log = LoggerFactory.getLogger(NewsTools.class);

    private final NewsRepository newsRepository;
    private final NewsEmbeddingsRepository newsEmbeddingsRepository;
    private final  EmbeddingService embeddingService;

    @Autowired
    public NewsTools(NewsRepository newsRepository, NewsEmbeddingsRepository newsEmbeddingsRepository,
                     EmbeddingService embeddingService){
        this.newsRepository=newsRepository;
        this.newsEmbeddingsRepository=newsEmbeddingsRepository;
        this.embeddingService=embeddingService;
    }

    @Tool(description="""
            Searches the news database for articles semantically similar to the user's query,for news based on meaning.
            Use this for vague topics like 'economic trends' or 'tech breakthroughs' where a specific tag might not exist.
            Use this when the user asks about a topic, wants to find relevant articles.
            Optionally filter by time window. Returns a list of matching news article IDs, titles, and excerpts.
            """)
    public NewsSearchResult searchNewsByTopic(
            @ToolParam(description="The topic or question to search for") String query,
            @ToolParam(description="Hours to look back. 24 = last day, 168 = last week, 720 = last month. 0 means no time filter.") int hoursBack,
            @ToolParam(description="Maximum number of articles to return (1-10)") int limit){

        log.info("Tool: searchNewsByTopic query='{}' hoursBack={} limit={}", query, hoursBack, limit);

        LocalDateTime since = hoursBack > 0 ? LocalDateTime.now().minusHours(hoursBack) : null;

        try{
            float[] queryVector = embeddingService.embedQuery(query);
            String vectorString = EmbeddingService.toVectorString(queryVector);

            int safelimit = Math.min(Math.max(limit,1),10);
            List<Long> ids = newsEmbeddingsRepository.findTopKSimilarNewsIds(vectorString,safelimit,since);
            if (ids.isEmpty()) return new NewsSearchResult(Collections.emptyList(), 0);

            List<News> newsList = newsRepository.findAllById(ids);
            Map<Long,News> newsMap = newsList.stream().collect(Collectors.toMap(News::getId,n->n));

            List<NewsSearchResult.Item> items = ids.stream()
                    .filter(newsMap::containsKey)
                    .map(newsMap::get)
                    .map(n-> new NewsSearchResult.Item(
                            n.getId(),
                            n.getTitle(),
                            excerpt(n.getContent()),
                            n.getCreatedDate() !=null ? n.getCreatedDate().toString() : "",
                            n.getAuthor() !=null ? n.getAuthor().getName() : "unknown",
                            n.getTags().stream().map(Tag::getName).collect(Collectors.joining(", "))
                    )).toList();
            return new NewsSearchResult(items,items.size());

        }catch(Exception e){
            log.error("Embedding failed for query '{}': {}", query, e.getMessage());
            return new NewsSearchResult(Collections.emptyList(), 0);
        }
    }

    @Tool(description = """
            Retrieves the most recent news articles filtered by tag/category.
            Use this for requests like 'latest tech news', 'what happened in sports today',
            'recent business articles'. Tags examples: tech, business, climate, economy, health.
            """)
    public NewsSearchResult getLatestNewsByTag(
            @ToolParam(description = "The tag/category name to filter by (e.g. 'tech', 'economy')") String tag,
            @ToolParam(description = "Hours to look back. 24 = last 24h, 168 = last week. 0 means last 10 articles regardless of time.") int hoursBack,
            @ToolParam(description = "Maximum number of articles (1-10)") int limit) {

        log.info("Tool: getLatestNewsByTag tag='{}' hoursBack={} limit={}", tag, hoursBack, limit);

        LocalDateTime since = hoursBack > 0 ? LocalDateTime.now().minusHours(hoursBack) : null;
        int safelimit = Math.min(Math.max(limit,1),10);

        List<News> results = since !=null
                ? newsRepository.findByTagNameAndCreatedDateAfter(tag,since, PageRequest.of(0,safelimit, Sort.by(Sort.Direction.DESC,"createdDate")))
                : newsRepository.findByTagName(tag,PageRequest.of(0,safelimit, Sort.by(Sort.Direction.DESC,"createdDate")));

        List<NewsSearchResult.Item> items = results.stream()
                .map(n->new NewsSearchResult.Item(
                        n.getId(),
                        n.getTitle(),
                        excerpt(n.getContent()),
                        n.getCreatedDate() !=null ? n.getCreatedDate().toString() : "",
                        n.getAuthor() !=null ? n.getAuthor().getName() : "unknown",
                        n.getTags().stream().map(Tag::getName).collect(Collectors.joining(", "))
                )).toList();

        return new NewsSearchResult(items,items.size());
    }

    @Tool(description = """
            Retrieves the full content of a specific news article by its ID.
            Use this when you need the complete text to summarize a specific article,
            or when the user says 'summarize the first one' after a search result.
            """)
    public ArticleDetail getFullArticle(
            @ToolParam(description = "The numeric ID of the news article") Long newsId) {

        log.info("Tool: getFullArticle newsId={}", newsId);

        return newsRepository.findById(newsId).map(n -> {
            String content = n.getContent() != null
                    ? n.getContent().replaceAll("<[^>]*>", "").trim()
                    : "";
            return new ArticleDetail(
                    n.getId(), n.getTitle(), content,
                    n.getAuthor() != null ? n.getAuthor().getName() : "Unknown",
                    n.getCreatedDate() != null ? n.getCreatedDate().toString() : "",
                    n.getTags().stream().map(Tag::getName).collect(Collectors.joining(", "))
            );
        }).orElse(new ArticleDetail(newsId, "Not found", "", "", "", ""));
    }

    @Tool(description = """
            Retrieves the top N most recent news articles across all topics for a digest or briefing.
            Use this for requests like 'give me today's top news', 'what happened today',
            'morning briefing', 'daily digest'.
            """)
    public NewsSearchResult getTopRecentNews(
            @ToolParam(description = "Hours to look back (e.g. 24 for today, 48 for yesterday+today)") int hoursBack,
            @ToolParam(description = "Number of top articles to include (3-10)") int limit) {

        log.info("Tool: getTopRecentNews hoursBack={} limit={}", hoursBack, limit);

        LocalDateTime since = LocalDateTime.now().minusHours(Math.max(hoursBack, 1));
        int safeLimit       = Math.min(Math.max(limit, 3), 10);

        List<News> results  = newsRepository.findByCreatedDateAfterOrderByCreatedDateDesc(
                since, PageRequest.of(0, safeLimit));

        List<NewsSearchResult.Item> items = results.stream()
                .map(n -> new NewsSearchResult.Item(
                        n.getId(), n.getTitle(), excerpt(n.getContent()),
                        n.getCreatedDate() != null ? n.getCreatedDate().toString() : "",
                        n.getAuthor()      != null ? n.getAuthor().getName()       : "Unknown",
                        n.getTags().stream().map(Tag::getName).collect(Collectors.joining(", "))
                )).toList();

        return new NewsSearchResult(items, items.size());
    }

        private static String excerpt(String html) {
        if (html == null) return "";
        String plain = html.replaceAll("<[^>]*>", "").trim();
        return plain.length() > 300 ? plain.substring(0, 300) + "…" : plain;
    }


}
