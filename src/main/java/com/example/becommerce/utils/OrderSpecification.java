package com.example.becommerce.utils;

import com.example.becommerce.dto.request.OrderSearchRequest;
import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.enums.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {

    private OrderSpecification() {}

    public static Specification<Order> buildFilter(OrderSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getStatus())) {
                try {
                    OrderStatus statusEnum = OrderStatus.valueOf(request.getStatus().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (StringUtils.hasText(request.getKeyword())) {
                String like = "%" + request.getKeyword().toLowerCase() + "%";
                Predicate codePredicate = cb.like(cb.lower(root.get("code")), like);
                Predicate servicePredicate = cb.like(cb.lower(root.get("serviceName")), like);
                predicates.add(cb.or(codePredicate, servicePredicate));
            }

            if (request.getCustomerId() != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), request.getCustomerId()));
            }

            if (request.getTechnicianId() != null) {
                predicates.add(cb.equal(root.get("technician").get("id"), request.getTechnicianId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
