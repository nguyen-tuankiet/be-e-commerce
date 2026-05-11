package com.example.becommerce.repository;

import com.example.becommerce.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByCode(String code);

    boolean existsByCode(String code);

    long count();

    /** Conversation between this exact (customer, technician) pair, regardless of order link. */
    Optional<Conversation> findByCustomer_IdAndTechnician_Id(Long customerId, Long technicianId);

    /** All conversations where the given user is either the customer or the technician. */
    @Query("""
            SELECT c FROM Conversation c
            WHERE c.customer.id = :userId OR c.technician.id = :userId
            """)
    Page<Conversation> findAllForUser(@Param("userId") Long userId, Pageable pageable);
}
