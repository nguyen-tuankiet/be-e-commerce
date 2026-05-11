package com.example.becommerce.utils;

import com.example.becommerce.entity.OrderReport;
import com.example.becommerce.entity.enums.ReportStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Composable filter predicates for the admin "list reports" endpoint.
 */
public final class OrderReportSpecification {

    private OrderReportSpecification() {}

    public static Specification<OrderReport> buildFilter(String status, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(status)) {
                try {
                    ReportStatus statusEnum = ReportStatus.from(status);
                    if (statusEnum != null) {
                        predicates.add(cb.equal(root.get("status"), statusEnum));
                    }
                } catch (IllegalArgumentException ignored) { /* invalid -> skip */ }
            }

            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.toLowerCase() + "%";
                Predicate code        = cb.like(cb.lower(root.get("code")),        like);
                Predicate description = cb.like(cb.lower(root.get("description")), like);
                predicates.add(cb.or(code, description));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
