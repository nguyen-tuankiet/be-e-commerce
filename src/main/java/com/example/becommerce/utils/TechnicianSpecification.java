package com.example.becommerce.utils;

import com.example.becommerce.entity.TechnicianProfile;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter predicates for the technician listing.
 *
 * <p>Notes
 *  <ul>
 *    <li>{@code service} matches against {@code service_category} OR any skill.</li>
 *    <li>{@code district} matches the user's district OR any of the technician's
 *        registered service areas.</li>
 *    <li>{@code minRating} is applied in Java after fetching, since rating is
 *        derived from the Review aggregate (avoid noisy SQL JOIN).</li>
 *  </ul>
 */
public final class TechnicianSpecification {

    private TechnicianSpecification() {}

    public static Specification<TechnicianProfile> buildFilter(
            String service, String district, Boolean isAvailable, String keyword) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Restrict to non-deleted users; the join also enables district/keyword filters.
            var userJoin = root.join("user", JoinType.INNER);
            predicates.add(cb.isFalse(userJoin.get("deleted")));

            if (isAvailable != null) {
                predicates.add(cb.equal(root.get("isAvailable"), isAvailable));
            }

            if (StringUtils.hasText(service)) {
                String like = "%" + service.toLowerCase() + "%";
                Predicate categoryMatch = cb.like(cb.lower(root.get("serviceCategory")), like);

                // Skills are an @ElementCollection; use a subquery to avoid duplicating rows.
                var skillJoin = root.join("skills", JoinType.LEFT);
                Predicate skillMatch = cb.like(cb.lower(skillJoin), like);
                if (query != null) query.distinct(true);

                predicates.add(cb.or(categoryMatch, skillMatch));
            }

            if (StringUtils.hasText(district)) {
                String like = "%" + district.toLowerCase() + "%";
                Predicate userDistrict = cb.like(cb.lower(userJoin.get("district")), like);
                var areaJoin = root.join("areas", JoinType.LEFT);
                Predicate areaMatch = cb.like(cb.lower(areaJoin), like);
                if (query != null) query.distinct(true);

                predicates.add(cb.or(userDistrict, areaMatch));
            }

            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.toLowerCase() + "%";
                Predicate fullName = cb.like(cb.lower(userJoin.get("fullName")), like);
                Predicate phone    = cb.like(userJoin.get("phone"), "%" + keyword + "%");
                predicates.add(cb.or(fullName, phone));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
