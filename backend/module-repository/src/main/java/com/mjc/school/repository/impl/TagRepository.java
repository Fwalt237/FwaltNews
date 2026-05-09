package com.mjc.school.repository.impl;

import com.mjc.school.repository.model.Tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TagRepository extends JpaRepository<Tag,Long>, JpaSpecificationExecutor<Tag> {

    @Query("SELECT t FROM Tag t INNER JOIN t.news n WHERE n.id =:newsId")
    List<Tag> findByNewsId(@Param("newsId") Long newsId);

    Optional<Tag> findByName(String name);

}
