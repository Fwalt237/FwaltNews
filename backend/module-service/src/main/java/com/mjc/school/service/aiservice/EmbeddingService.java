package com.mjc.school.service.aiservice;

import com.mjc.school.repository.airepo.model.NewsEmbeddings;
import com.mjc.school.repository.model.News;
import com.mjc.school.repository.airepo.impl.NewsEmbeddingsRepository;
import com.mjc.school.repository.impl.NewsRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final MyEmbeddingClient embeddingClient;
    private final NewsEmbeddingsRepository newsEmbeddingsRepository;
    private final NewsRepository newsRepository;


    @Autowired
    public EmbeddingService(MyEmbeddingClient embeddingClient,NewsEmbeddingsRepository newsEmbeddingsRepository,
                            NewsRepository newsRepository){
        this.embeddingClient=embeddingClient;
        this.newsEmbeddingsRepository=newsEmbeddingsRepository;
        this.newsRepository=newsRepository;
    }

    @Async
    @Transactional
    public void embedNews(Long newsId){
        if(newsEmbeddingsRepository.existsByNewsId(newsId)) return;

        newsRepository.findById(newsId).ifPresent(news->{
           try{
               float[] vector = embeddingClient.getEmbedding(buildText(news));
               NewsEmbeddings ne = new NewsEmbeddings();
               ne.setNews(news);
               ne.setEmbedding(vector);
               newsEmbeddingsRepository.save(ne);
               log.debug("Embedded news of id = {}",newsId);
           }catch(Exception e){
               log.error("Failed to embed news of id={} : {}",newsId,e.getMessage());
           }
        });
    }

    @Async
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void embedMissing() {
        List<News> missing = newsRepository.findNewsWithoutEmbeddings();
        log.info("Found {} articles missing embeddings. Processing...", missing.size());

        for (News news : missing) {
            this.embedNews(news.getId());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String buildText(News news){
        String content = news.getContent()!=null
                ? news.getContent().replaceAll("<[^>]*>","").trim()
                :"";
        return news.getTitle() + "\n\n" + content;
    }

    public float[] embedQuery(String query){
        return embeddingClient.getEmbedding(query);
    }

    public static String toVectorString(float[] vector){

        if(vector==null) return "[]";

        return IntStream.range(0,vector.length)
                .mapToObj(i->String.format("%.8f",vector[i]))
                .collect(Collectors.joining(",","[","]"));
    }

}
