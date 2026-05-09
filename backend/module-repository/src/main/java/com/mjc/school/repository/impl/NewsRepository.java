package com.mjc.school.repository.impl;


import com.mjc.school.repository.model.News;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;


@Repository
public interface NewsRepository extends JpaRepository<News,Long>, JpaSpecificationExecutor<News> {

    boolean existsByTitle(String title);

    @Modifying
    @Query("DELETE FROM News n WHERE n.createdDate < :date")
    void deleteOlderThan(LocalDateTime date);

    @Query("SELECT n FROM News n JOIN n.tags t WHERE LOWER(t.name) = LOWER(:tagName)")
    List<News> findByTagName(@Param("tagName") String tagName, Pageable pageable);

    @Query("SELECT n FROM News n JOIN n.tags t " +
            "WHERE LOWER(t.name) = LOWER(:tagName) AND n.createdDate >= :since")
    List<News> findByTagNameAndCreatedDateAfter(
            @Param("tagName") String tagName,
            @Param("since")   LocalDateTime since,
            Pageable pageable);

    @Query("SELECT n FROM News n WHERE n.createdDate >= :since ORDER BY n.createdDate DESC")
    List<News> findByCreatedDateAfterOrderByCreatedDateDesc(
            @Param("since") LocalDateTime since,
            Pageable pageable);

    @Query("SELECT n FROM News n LEFT JOIN NewsEmbeddings ne ON n.id = ne.news.id WHERE ne.id IS NULL")
    List<News> findNewsWithoutEmbeddings();
}
