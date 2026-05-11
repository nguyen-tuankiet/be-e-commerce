package com.example.becommerce.utils;

import com.example.becommerce.entity.Verification;
import com.example.becommerce.entity.enums.VerificationStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Composable predicates for the admin "list verifications" endpoint.
 */
public final class VerificationSpecification {

    private VerificationSpecification() {}

    public static Specification<Verification> buildFilter(String status, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(status)) {
                try {
                    VerificationStatus statusEnum = VerificationStatus.from(status);
                    if (statusEnum != null) {
                        predicates.add(cb.equal(root.get("status"), statusEnum));
                    }
                } catch (IllegalArgumentException ignored) { /* skip invalid */ }
            }

            if (StringUtils.hasText(keyword)) {
                var techJoin = root.join("technician", JoinType.LEFT);
                String like = "%" + keyword.toLowerCase() + "%";
                Predicate code     = cb.like(cb.lower(root.get("code")), like);
                Predicate fullName = cb.like(cb.lower(techJoin.get("fullName")), like);
                Predicate email    = cb.like(cb.lower(techJoin.get("email")),    like);
                predicates.add(cb.or(code, fullName, email));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
