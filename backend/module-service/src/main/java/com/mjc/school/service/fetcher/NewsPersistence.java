package com.mjc.school.service.fetcher;

import com.mjc.school.repository.impl.AuthorRepository;
import com.mjc.school.repository.impl.NewsRepository;
import com.mjc.school.repository.impl.TagRepository;
import com.mjc.school.repository.model.Author;
import com.mjc.school.repository.model.News;
import com.mjc.school.repository.model.Tag;
import com.mjc.school.service.aiservice.EmbeddingService;
import com.mjc.school.service.dto.NewsDataItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class NewsPersistence {

    private final NewsRepository newsRepository;
    private final AuthorRepository authorRepository;
    private final TagRepository tagRepository;
    private final ArticleScraper scraper;
    private final EmbeddingService embeddingService;

    @Autowired
    public NewsPersistence(NewsRepository newsRepository, AuthorRepository authorRepository,
                           TagRepository tagRepository, ArticleScraper scraper,
                           EmbeddingService embeddingService){
        this.newsRepository=newsRepository;
        this.authorRepository=authorRepository;
        this.tagRepository=tagRepository;
        this.scraper=scraper;
        this.embeddingService=embeddingService;
    }

    @Transactional
    public void persist(NewsDataItem item) {

        String finalAuthorName = "Unknown";
        if (item.creator() instanceof List<?> creators && !creators.isEmpty()) {
            finalAuthorName = creators.get(0).toString();
        } else if (item.creator() instanceof String s && !s.contains("ONLY AVAILABLE")) {
            finalAuthorName = s;
        }

        String nameToUse = finalAuthorName;
        Author author = authorRepository.findByName(nameToUse)
                .orElseGet(() -> {
                    Author newAuthor = new Author();
                    newAuthor.setName(nameToUse);
                    return authorRepository.save(newAuthor);
                });

        News news = new News();
        news.setTitle(item.title());
        news.setAuthor(author);
        news.setImageUrl(item.imageUrl());
        news.setSourceIcon(item.sourceIcon());

        List<Tag> tags = new ArrayList<>();
        if (item.category() instanceof List<?> rawCategory) {
            for (Object obj : rawCategory) {
                String tagName = obj.toString();
                tags.add(tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName))));
            }
        }
        news.setTags(tags);

        String body = scraper.scrape(item.link());
        if (body == null || body.isBlank()) {
            body = item.content() != null ? item.content()
                    : item.description() != null ? item.description()
                    : "No content available for this article.";
        }
        news.setContent(body);

        News savedNews = newsRepository.save(news);
        embeddingService.embedNews(savedNews.getId());
    }
}
