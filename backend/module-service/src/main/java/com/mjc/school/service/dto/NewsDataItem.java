package com.mjc.school.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown=true)
public record NewsDataItem (
        @JsonProperty("article_id")
        String articleId,

        String title,
        String link,
        String description,
        String content,
        Object  creator,
        Object category,

        @JsonProperty("image_url")
        String imageUrl,

        @JsonProperty("source_icon")
        String sourceIcon,

        @JsonProperty("pubDate")
        String pubDate
){
}
