package com.example.demo.repository;

import com.example.demo.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    // Get conversation between two users about a property
    @Query("SELECT m FROM ChatMessage m WHERE " +
            "m.property.id = :propId AND " +
            "((m.sender.id = :u1 AND m.receiver.id = :u2) OR " +
            "(m.sender.id = :u2 AND m.receiver.id = :u1)) " +
            "ORDER BY m.sentAt ASC")
    List<ChatMessage> findConversation(
            @Param("propId") Long propId,
            @Param("u1") Long u1,
            @Param("u2") Long u2);

    // Get all chat threads for a user
    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.sender.id = :userId OR m.receiver.id = :userId) " +
            "ORDER BY m.sentAt DESC")
    List<ChatMessage> findAllForUser(@Param("userId") Long userId);

    // Unread count
    long countByReceiverIdAndIsReadFalse(Long receiverId);

    // Mark conversation as read
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE " +
            "m.receiver.id = :userId AND m.sender.id = :senderId " +
            "AND m.property.id = :propId")
    void markConversationRead(
            @Param("userId") Long userId,
            @Param("senderId") Long senderId,
            @Param("propId") Long propId);
}