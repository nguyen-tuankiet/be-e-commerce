package com.example.becommerce.repository;

import com.example.becommerce.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    boolean existsByCode(String code);

    long count();

    Page<Message> findByConversation_IdOrderBySentAtDesc(Long conversationId, Pageable pageable);

    @EntityGraph(attributePaths = {"sender", "quotation"})
    Optional<Message> findTopByConversation_IdOrderBySentAtDesc(Long conversationId);

    /**
     * Unread count when the participant has never opened the thread (no watermark).
     */
    @Query("""
            SELECT COUNT(m) FROM Message m
            WHERE m.conversation.id = :conversationId
              AND (m.sender IS NULL OR m.sender.id <> :userId)
            """)
    long countUnreadAll(@Param("conversationId") Long conversationId,
                        @Param("userId") Long userId);

    /**
     * Unread count after the participant's last-read watermark.
     */
    @Query("""
            SELECT COUNT(m) FROM Message m
            WHERE m.conversation.id = :conversationId
              AND (m.sender IS NULL OR m.sender.id <> :userId)
              AND m.sentAt > :watermark
            """)
    long countUnreadSince(@Param("conversationId") Long conversationId,
                          @Param("userId") Long userId,
                          @Param("watermark") LocalDateTime watermark);
}
