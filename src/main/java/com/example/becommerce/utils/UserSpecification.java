package com.example.becommerce.utils;

import com.example.becommerce.entity.User;
import com.example.becommerce.entity.enums.Role;
import com.example.becommerce.entity.enums.UserStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic User filtering.
 * Enables type-safe, composable query predicates without raw JPQL.
 */
public class UserSpecification {

    private UserSpecification() {}

    /**
     * Build a combined Specification from optional filter parameters.
     *
     * @param role     optional role name string
     * @param status   optional status name string
     * @param district optional district filter
     * @param keyword  optional keyword for fullName / email / phone search
     */
    public static Specification<User> buildFilter(
            String role, String status, String district, String keyword) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted users
            predicates.add(cb.isFalse(root.get("deleted")));

            // Role filter
            if (StringUtils.hasText(role)) {
                try {
                    Role roleEnum = Role.valueOf(role.toUpperCase());
                    predicates.add(cb.equal(root.get("role"), roleEnum));
                } catch (IllegalArgumentException ignored) { /* invalid role — skip */ }
            }

            // Status filter
            if (StringUtils.hasText(status)) {
                try {
                    UserStatus statusEnum = UserStatus.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) { /* invalid status — skip */ }
            }

            // District filter — case-insensitive contains
            if (StringUtils.hasText(district)) {
                predicates.add(cb.like(
                        cb.lower(root.get("district")),
                        "%" + district.toLowerCase() + "%"));
            }

            // Keyword search: fullName, email, or phone
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.toLowerCase() + "%";
                Predicate namePredicate  = cb.like(cb.lower(root.get("fullName")), like);
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")),    like);
                Predicate phonePredicate = cb.like(root.get("phone"),              "%" + keyword + "%");
                predicates.add(cb.or(namePredicate, emailPredicate, phonePredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
