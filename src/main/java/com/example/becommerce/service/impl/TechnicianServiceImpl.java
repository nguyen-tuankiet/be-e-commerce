package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.ReviewMapper;
import com.example.becommerce.dto.mapper.TechnicianMapper;
import com.example.becommerce.dto.request.technician.UpdateAvailabilityRequest;
import com.example.becommerce.dto.request.technician.UpdateTechnicianProfileRequest;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.review.ReviewResponse;
import com.example.becommerce.dto.response.technician.AvailabilityResponse;
import com.example.becommerce.dto.response.technician.TechnicianDetailResponse;
import com.example.becommerce.dto.response.technician.TechnicianListItemResponse;
import com.example.becommerce.dto.response.technician.TechnicianProfileUpdateResponse;
import com.example.becommerce.dto.response.technician.TechnicianReviewListResponse;
import com.example.becommerce.entity.Review;
import com.example.becommerce.entity.TechnicianProfile;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.enums.OrderStatus;
import com.example.becommerce.entity.enums.Role;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.OrderRepository;
import com.example.becommerce.repository.ReviewRepository;
import com.example.becommerce.repository.TechnicianProfileRepository;
import com.example.becommerce.repository.UserRepository;
import com.example.becommerce.service.TechnicianService;
import com.example.becommerce.utils.TechnicianSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Technician business operations.
 *
 * <p>Profile creation strategy: each technician's profile row is lazily
 * created the first time it's needed, so legacy users that registered
 * before this module existed don't break.
 *
 * <p>Aggregate metrics — average rating, review count, completed jobs —
 * are computed on-demand against the Review and Order tables. For high
 * traffic these should be denormalized; for now correctness over speed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TechnicianServiceImpl implements TechnicianService {

    private final TechnicianProfileRepository profileRepository;
    private final UserRepository              userRepository;
    private final ReviewRepository            reviewRepository;
    private final OrderRepository             orderRepository;
    private final TechnicianMapper            technicianMapper;
    private final ReviewMapper                reviewMapper;

    // ===============================================================
    // LIST
    // ===============================================================

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TechnicianListItemResponse> listTechnicians(
            String service, String district, Boolean isAvailable, Double minRating,
            String keyword, int page, int limit) {

        Specification<TechnicianProfile> spec =
                TechnicianSpecification.buildFilter(service, district, isAvailable, keyword);

        Pageable pageable = PageRequest.of(
                Math.max(0, page - 1),
                limit,
                Sort.by(Sort.Direction.DESC, "joinedAt"));

        Page<TechnicianProfile> profiles = profileRepository.findAll(spec, pageable);

        List<TechnicianListItemResponse> items = profiles.getContent().stream()
                .map(this::buildListItem)
                .filter(item -> minRating == null
                        || (item.getRating() != null && item.getRating().doubleValue() >= minRating))
                .toList();

        return PagedResponse.of(items, page, limit, profiles.getTotalElements());
    }

    // ===============================================================
    // DETAIL
    // ===============================================================

    @Override
    @Transactional(readOnly = true)
    public TechnicianDetailResponse getTechnician(String code) {
        User technician = findTechnicianUser(code);
        TechnicianProfile profile = findOrCreateProfile(technician);

        Long techId = technician.getId();
        Double avg = reviewRepository.averageRatingByTechnician(techId);
        long reviews = reviewRepository.countByTechnician_Id(techId);
        long completed = orderRepository.countByTechnician_IdAndStatusAndDeletedFalse(
                techId, OrderStatus.COMPLETED);

        return technicianMapper.toDetail(profile, avg, reviews, completed);
    }

    // ===============================================================
    // UPDATE PROFILE
    // ===============================================================

    @Override
    @Transactional
    public TechnicianProfileUpdateResponse updateProfile(String code, UpdateTechnicianProfileRequest request) {
        User technician = findTechnicianUser(code);
        ensureSelfOrAdmin(technician);

        TechnicianProfile profile = findOrCreateProfile(technician);

        // ---- Apply user-side fields (only if provided) -------------
        if (StringUtils.hasText(request.getFullName()))         technician.setFullName(request.getFullName());
        if (StringUtils.hasText(request.getPhone()))            technician.setPhone(request.getPhone());
        if (StringUtils.hasText(request.getEmail()))            technician.setEmail(request.getEmail().toLowerCase());
        if (StringUtils.hasText(request.getAvatar()))           technician.setAvatar(request.getAvatar());
        if (StringUtils.hasText(request.getDistrict()))         technician.setDistrict(request.getDistrict());
        if (StringUtils.hasText(request.getBio()))              technician.setBio(request.getBio());
        userRepository.save(technician);

        // ---- Apply profile-side fields -----------------------------
        if (StringUtils.hasText(request.getCoverImage()))       profile.setCoverImage(request.getCoverImage());
        if (StringUtils.hasText(request.getServiceCategory()))  profile.setServiceCategory(request.getServiceCategory());
        if (request.getPricePerHour() != null)                  profile.setPricePerHour(request.getPricePerHour());
        if (request.getYearsExperience() != null)               profile.setYearsExperience(request.getYearsExperience());

        if (request.getSkills() != null) {
            profile.getSkills().clear();
            request.getSkills().stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .forEach(profile.getSkills()::add);
        }
        if (request.getAreas() != null) {
            profile.getAreas().clear();
            request.getAreas().stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .forEach(profile.getAreas()::add);
        }
        if (request.getSchedule() != null) {
            // Replace whole schedule with provided one (null values clear that day).
            Map<String, String> newSchedule = new HashMap<>();
            request.getSchedule().forEach((day, time) -> {
                if (StringUtils.hasText(day) && StringUtils.hasText(time)) {
                    newSchedule.put(day.toLowerCase().trim(), time.trim());
                }
            });
            profile.getSchedule().clear();
            profile.getSchedule().putAll(newSchedule);
        }

        TechnicianProfile saved = profileRepository.save(profile);
        log.info("Technician profile updated for {}", technician.getCode());
        return technicianMapper.toUpdateResponse(saved);
    }

    // ===============================================================
    // AVAILABILITY
    // ===============================================================

    @Override
    @Transactional
    public AvailabilityResponse updateAvailability(String code, UpdateAvailabilityRequest request) {
        User technician = findTechnicianUser(code);
        ensureSelfOrAdmin(technician);
        TechnicianProfile profile = findOrCreateProfile(technician);

        profile.setAvailable(Boolean.TRUE.equals(request.getIsAvailable()));
        TechnicianProfile saved = profileRepository.save(profile);
        return technicianMapper.toAvailabilityResponse(saved);
    }

    // ===============================================================
    // REVIEWS
    // ===============================================================

    @Override
    @Transactional(readOnly = true)
    public TechnicianReviewListResponse listReviews(String code, int page, int limit) {
        User technician = findTechnicianUser(code);
        Long techId = technician.getId();

        Pageable pageable = PageRequest.of(
                Math.max(0, page - 1),
                limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Review> reviews = reviewRepository.findByTechnician_IdOrderByCreatedAtDesc(techId, pageable);

        List<ReviewResponse> items = reviews.getContent().stream()
                .map(reviewMapper::toResponse)
                .toList();

        Double avg = reviewRepository.averageRatingByTechnician(techId);
        long total = reviewRepository.countByTechnician_Id(techId);

        return TechnicianReviewListResponse.builder()
                .averageRating(avg == null || avg == 0d
                        ? null
                        : BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP))
                .totalReviews(total)
                .items(items)
                .pagination(PagedResponse.PaginationMeta.builder()
                        .page(page)
                        .limit(limit)
                        .total(reviews.getTotalElements())
                        .totalPages((int) Math.ceil((double) reviews.getTotalElements() / limit))
                        .build())
                .build();
    }

    // ===============================================================
    // Helpers
    // ===============================================================

    private TechnicianListItemResponse buildListItem(TechnicianProfile profile) {
        Long techId = profile.getUser().getId();
        Double avg = reviewRepository.averageRatingByTechnician(techId);
        long reviews = reviewRepository.countByTechnician_Id(techId);
        long completed = orderRepository.countByTechnician_IdAndStatusAndDeletedFalse(
                techId, OrderStatus.COMPLETED);
        return technicianMapper.toListItem(profile, avg, reviews, completed);
    }

    private User findTechnicianUser(String code) {
        User user = userRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy thợ " + code));
        if (user.getRole() != Role.TECHNICIAN) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR,
                    "Tài khoản không phải tài khoản thợ");
        }
        return user;
    }

    private TechnicianProfile findOrCreateProfile(User technician) {
        return profileRepository.findByUser_Id(technician.getId())
                .orElseGet(() -> profileRepository.save(
                        TechnicianProfile.builder().user(technician).build()));
    }

    private void ensureSelfOrAdmin(User target) {
        User current = getCurrentUser();
        if (current.getRole() == Role.ADMIN) return;
        if (current.getId().equals(target.getId())) return;
        throw AppException.forbidden("Bạn không có quyền chỉnh sửa hồ sơ thợ khác");
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw AppException.unauthorized("Người dùng chưa đăng nhập");
        }
        String email = authentication.getName();
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy người dùng hiện tại"));
    }
}
