package com.example.becommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * An image attached to an order. Distinguishes between
 * the initial request images ({@code role="request"}) and
 * completion proof images ({@code role="completion"}).
 */
@Entity
@Table(name = "order_images",
        indexes = @Index(name = "idx_order_images_order", columnList = "order_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 500)
    private String url;

    /** "request" | "completion" | "evidence" */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String role = "request";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
