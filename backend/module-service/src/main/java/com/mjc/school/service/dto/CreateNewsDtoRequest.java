package com.mjc.school.service.dto;

import com.mjc.school.service.validator.constraint.NotNull;
import com.mjc.school.service.validator.constraint.Size;

import java.util.ArrayList;
import java.util.List;

public record CreateNewsDtoRequest(
    @NotNull
    @Size(min = 6, max = 1000)
    String title,

    @NotNull
    @Size(min = 12)
    String content,

    String imageUrl,
    String sourceIcon,

    @NotNull
    String author,

    List<String> tags,

    List<Long> commentsIds
) {
     public CreateNewsDtoRequest {
         if (tags == null) {
             tags = new ArrayList<>();
         }
         if (commentsIds == null) {
            commentsIds = new ArrayList<>();
         }
     }
}
