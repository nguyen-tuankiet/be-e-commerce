package com.example.becommerce.utils;

import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.enums.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic Order filtering.
 * Used by the order list endpoint with optional filters per actor.
 */
public final class OrderSpecification {

    private OrderSpecification() {}

    /**
     * Composite filter.
     *
     * @param status      optional status (raw API value, hyphen-separated tolerated)
     * @param customerId  if non-null, restrict to this customer's orders
     * @param technicianId if non-null, restrict to this technician's orders
     * @param keyword     fuzzy search across code / serviceName / address
     */
    public static Specification<Order> buildFilter(String status,
                                                    Long customerId,
                                                    Long technicianId,
                                                    String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("deleted")));

            if (StringUtils.hasText(status)) {
                try {
                    OrderStatus orderStatus = OrderStatus.from(status);
                    if (orderStatus != null) {
                        predicates.add(cb.equal(root.get("status"), orderStatus));
                    }
                } catch (IllegalArgumentException ignored) { /* invalid status — skip */ }
            }

            if (customerId != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            }

            if (technicianId != null) {
                predicates.add(cb.equal(root.get("technician").get("id"), technicianId));
            }

            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.toLowerCase() + "%";
                Predicate code         = cb.like(cb.lower(root.get("code")),        like);
                Predicate service      = cb.like(cb.lower(root.get("serviceName")), like);
                Predicate address      = cb.like(cb.lower(root.get("address")),     like);
                predicates.add(cb.or(code, service, address));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
