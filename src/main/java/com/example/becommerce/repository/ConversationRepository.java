package com.example.becommerce.repository;

import com.example.becommerce.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    @Query("SELECT c FROM Conversation c WHERE c.order.customer.id = :userId OR c.order.technician.id = :userId ORDER BY c.updatedAt DESC")
    List<Conversation> findAllByParticipantId(@Param("userId") Long userId);
}
