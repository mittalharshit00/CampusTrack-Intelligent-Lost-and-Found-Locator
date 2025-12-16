package com.campuslfp.repository;

import com.campuslfp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversation_IdOrderBySentAtAsc(Long conversationId);
    long countByConversation_IdAndSender_Id(Long conversationId, Long senderId);
}
