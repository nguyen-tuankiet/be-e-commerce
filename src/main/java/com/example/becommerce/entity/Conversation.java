package com.example.becommerce.entity;

import com.example.becommerce.entity.enums.ConversationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Chat conversation between exactly one customer and one technician.
 * Optionally bound to an order.
 *
 * <p>Read tracking uses a watermark per participant:
 * {@code customerLastReadAt} / {@code technicianLastReadAt}. The unread
 * counter is derived from {@code messages.sentAt > watermark}.
 */
@Entity
@Table(name = "conversations",
        indexes = {
                @Index(name = "idx_conv_code",       columnList = "code", unique = true),
                @Index(name = "idx_conv_customer",   columnList = "customer_id"),
                @Index(name = "idx_conv_technician", columnList = "technician_id"),
                @Index(name = "idx_conv_order",      columnList = "order_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** e.g. CONV-001 */
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @ToString.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technician_id", nullable = false)
    @ToString.Exclude
    private User technician;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConversationStatus status = ConversationStatus.ACTIVE;

    @Column(name = "customer_last_read_at")
    private LocalDateTime customerLastReadAt;

    @Column(name = "technician_last_read_at")
    private LocalDateTime technicianLastReadAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
