package com.mjc.school.repository.impl;

import com.mjc.school.repository.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    @Query("SELECT c FROM Comment c INNER JOIN c.news n WHERE n.id =:newsId")
    List<Comment> findByNewsId(@Param("newsId") Long newsId);

}
