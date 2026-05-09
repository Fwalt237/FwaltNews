package com.mjc.school.repository.airepo.model;


import com.mjc.school.repository.model.News;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name="news_embeddings")
public class NewsEmbeddings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="news_id",nullable=false,unique=true)
    private News news;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(name = "embedding", columnDefinition = "vector(768)", nullable = false)
    private float[] embedding;

    @Column(name="embedded_at",nullable=false)
    private LocalDateTime embeddedAt;

    @PrePersist
    void prePersist() {
        this.embeddedAt=LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public News getNews() {
        return news;
    }

    public void setNews(News news) {
        this.news = news;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public LocalDateTime getEmbeddedAt() {
        return embeddedAt;
    }

    public void setEmbeddedAt(LocalDateTime embeddedAt) {
        this.embeddedAt = embeddedAt;
    }
}
