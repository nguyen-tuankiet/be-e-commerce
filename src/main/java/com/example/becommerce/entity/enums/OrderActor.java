package com.example.becommerce.entity.enums;

/**
 * Actor who performs actions on orders.
 * Mapped to schema 'order_actor' enum.
 */
public enum OrderActor {
    CUSTOMER,
    TECHNICIAN,
    ADMIN,
    SYSTEM
}
