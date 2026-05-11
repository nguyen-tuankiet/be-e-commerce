package com.example.becommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single replacement part declared inside a price adjustment.
 */
@Entity
@Table(name = "order_price_adjustment_parts",
        indexes = @Index(name = "idx_adj_part_adj", columnList = "adjustment_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPriceAdjustmentPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adjustment_id", nullable = false)
    private OrderPriceAdjustment adjustment;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Column(name = "part_code", length = 50)
    private String partCode;
}
