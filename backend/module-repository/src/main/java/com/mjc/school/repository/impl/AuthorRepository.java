package com.mjc.school.repository.impl;

import com.mjc.school.repository.model.Author;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author,Long>, JpaSpecificationExecutor<Author> {

    @Query("SELECT a FROM Author a INNER JOIN a.news n WHERE n.id=:newsId")
    Optional<Author> findByNewsId(@Param("newsId")Long newsId);

    Optional<Author> findByName(String name);

}
