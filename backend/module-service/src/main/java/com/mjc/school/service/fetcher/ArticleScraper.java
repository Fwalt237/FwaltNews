package com.mjc.school.service.fetcher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ArticleScraper {

    private static final Logger log = LoggerFactory.getLogger(ArticleScraper.class);

    public String scrape(String url){
        try{
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/122.0.0.0 Safari/537.36")
                    .header("Accept-Language","en-US,en;q=0.9")
                    .header("Referer", "https://www.google.com/")
                    .timeout(5000)
                    .get();

            for (String selector : new String[]{"article", "[itemprop=articleBody]",
                    ".article-body", ".article-content",
                    ".story-body", ".post-content",
                    "main p", ".content p"}){
                String text = doc.select(selector).text();

                if(text.length() >200){
                    return text;
                }
            }

            String fallback = doc.select("p").text();
            return fallback.length() > 200 ? fallback : null;
        }catch(Exception e){
            log.warn("Scraping failed for {}: {}", url,e.getMessage());
            return null;
        }
    }



}
