package com.mjc.school.repository.airepo.impl;

import com.mjc.school.repository.airepo.model.ChatMessages;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessagesRepository extends JpaRepository<ChatMessages,Long> {

    List<ChatMessages> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    void deleteBySessionId(String sessionId);
}
