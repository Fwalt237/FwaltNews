package com.mjc.school.repository.airepo.impl;

import com.mjc.school.repository.airepo.model.NewsEmbeddings;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsEmbeddingsRepository extends JpaRepository<NewsEmbeddings,Long> {

    Optional<NewsEmbeddings> findByNewsId(Long newsId);

    boolean existsByNewsId(Long newsId);

    @Query(value= """
            SELECT ne.news_id
            FROM news_embeddings ne
            JOIN news n ON n.id = ne.news_id
            WHERE (CAST(:since AS timestamp) IS NULL OR n.created_date>=CAST(:since AS timestamp))
            ORDER BY ne.embedding <=> CAST(:queryVector AS vector)
            LIMIT :topK
            """,nativeQuery=true)
    List<Long> findTopKSimilarNewsIds(
            @Param("queryVector") String queryVector,
            @Param("topK") int topK,
            @Param("since")LocalDateTime since
    );


}
