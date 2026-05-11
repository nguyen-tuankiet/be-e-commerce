package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.technician.AvailabilityResponse;
import com.example.becommerce.dto.response.technician.TechnicianDetailResponse;
import com.example.becommerce.dto.response.technician.TechnicianListItemResponse;
import com.example.becommerce.dto.response.technician.TechnicianProfileUpdateResponse;
import com.example.becommerce.entity.TechnicianProfile;
import com.example.becommerce.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Maps the {@link TechnicianProfile} (joined with its {@link User}) to API DTOs.
 *
 * <p>Computed metrics ({@code rating}, {@code reviewCount}, {@code completedJobs})
 * are NOT stored on the profile — they are passed in by the service layer
 * after querying the relevant aggregate repositories. This keeps the mapper
 * pure and avoids hidden DB calls from inside it.
 */
@Component
public class TechnicianMapper {

    public TechnicianListItemResponse toListItem(TechnicianProfile profile,
                                                  Double avgRating,
                                                  long reviewCount,
                                                  long completedJobs) {
        if (profile == null) return null;
        User user = profile.getUser();

        return TechnicianListItemResponse.builder()
                .id(user == null ? null : user.getCode())
                .fullName(user == null ? null : user.getFullName())
                .avatar(user == null ? null : user.getAvatar())
                .rating(roundRating(avgRating))
                .reviewCount(reviewCount)
                .location(user == null ? null : user.getAddress())
                .district(user == null ? null : user.getDistrict())
                .skills(emptyToNull(profile.getSkills()))
                .pricePerHour(profile.getPricePerHour())
                .isAvailable(profile.isAvailable())
                .timeAvailable(profile.getTimeAvailable())
                .type(profile.getType() == null ? null : profile.getType().apiValue())
                .titleBadge(profile.getTitleBadge())
                .completedJobs(completedJobs)
                .build();
    }

    public TechnicianDetailResponse toDetail(TechnicianProfile profile,
                                              Double avgRating,
                                              long reviewCount,
                                              long completedJobs) {
        if (profile == null) return null;
        User user = profile.getUser();

        return TechnicianDetailResponse.builder()
                .id(user == null ? null : user.getCode())
                .fullName(user == null ? null : user.getFullName())
                .avatar(user == null ? null : user.getAvatar())
                .coverImage(profile.getCoverImage())
                .phone(user == null ? null : user.getPhone())
                .email(user == null ? null : user.getEmail())
                .location(user == null ? null : user.getAddress())
                .district(user == null ? null : user.getDistrict())
                .bio(user == null ? null : user.getBio())
                .skills(emptyToNull(profile.getSkills()))
                .rating(roundRating(avgRating))
                .reviewCount(reviewCount)
                .completedJobs(completedJobs)
                .isAvailable(profile.isAvailable())
                .type(profile.getType() == null ? null : profile.getType().apiValue())
                .titleBadge(profile.getTitleBadge())
                .verificationStatus(profile.getVerificationStatus() == null ? null : profile.getVerificationStatus().apiValue())
                .yearsExperience(profile.getYearsExperience())
                .schedule(emptyMapToNull(profile.getSchedule()))
                .joinedAt(profile.getJoinedAt())
                .lastActiveAt(profile.getLastActiveAt())
                .build();
    }

    public TechnicianProfileUpdateResponse toUpdateResponse(TechnicianProfile profile) {
        User user = profile.getUser();
        return TechnicianProfileUpdateResponse.builder()
                .id(user == null ? null : user.getCode())
                .fullName(user == null ? null : user.getFullName())
                .bio(user == null ? null : user.getBio())
                .skills(emptyToNull(profile.getSkills()))
                .areas(emptyToNull(profile.getAreas()))
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    public AvailabilityResponse toAvailabilityResponse(TechnicianProfile profile) {
        return AvailabilityResponse.builder()
                .id(profile.getUser() == null ? null : profile.getUser().getCode())
                .isAvailable(profile.isAvailable())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    // ---------------------------------------------------------------

    private BigDecimal roundRating(Double value) {
        if (value == null || value == 0d) return null;
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);
    }

    private <T> List<T> emptyToNull(List<T> list) {
        return list == null || list.isEmpty() ? null : list;
    }

    private <K, V> Map<K, V> emptyMapToNull(Map<K, V> map) {
        return map == null || map.isEmpty() ? null : map;
    }
}
