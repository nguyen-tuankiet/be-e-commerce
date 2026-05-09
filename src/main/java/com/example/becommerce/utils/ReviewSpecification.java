package com.example.becommerce.utils;

import com.example.becommerce.entity.Review;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ReviewSpecification {

    private ReviewSpecification() {}

    public static Specification<Review> buildFilter(Integer rating, Long technicianId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (rating != null) {
                predicates.add(cb.equal(root.get("rating"), rating));
            }

            if (technicianId != null) {
                predicates.add(cb.equal(root.get("technicianId"), technicianId));
            }

            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
