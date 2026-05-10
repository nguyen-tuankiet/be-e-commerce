package com.example.becommerce.service;

import com.example.becommerce.dto.request.TechnicianAvailabilityRequest;
import com.example.becommerce.dto.request.TechnicianRegisterRequest;
import com.example.becommerce.dto.request.TechnicianUpdateProfileRequest;
import com.example.becommerce.dto.response.*;
import com.example.becommerce.entity.TechnicianProfile;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.enums.KycStatus;
import com.example.becommerce.entity.enums.TechnicianStatus;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.OrderRepository;
import com.example.becommerce.repository.ReviewRepository;
import com.example.becommerce.repository.TechnicianProfileRepository;
import com.example.becommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing technician profiles and operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TechnicianService {

    private final TechnicianProfileRepository technicianProfileRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    /**
     * Đăng ký làm thợ.
     * Tạo TechnicianProfile cho user hiện tại.
     * Status sẽ là PENDING_KYC.
     */
    @Transactional
    public TechnicianRegisterResponse registerTechnician(Long userId, TechnicianRegisterRequest request) {
        log.info("Registering technician for user: {}", userId);

        // Kiểm tra user tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "User not found"));

        // Kiểm tra user đã có TechnicianProfile chưa
        if (technicianProfileRepository.existsByUserId(userId)) {
            throw new AppException("TECHNICIAN_ALREADY_EXISTS", 
                    "User already has a technician profile");
        }

        // Validate input
        if (request.getBasePrice() != null && request.getBasePrice() < 0) {
            throw new AppException("INVALID_PRICE", "Base price cannot be negative");
        }
        if (request.getHourlyRate() != null && request.getHourlyRate() < 0) {
            throw new AppException("INVALID_PRICE", "Hourly rate cannot be negative");
        }
        if (request.getExperienceYears() != null && request.getExperienceYears() < 0) {
            throw new AppException("INVALID_EXPERIENCE", "Experience years cannot be negative");
        }

        // Tạo TechnicianProfile
        TechnicianProfile profile = TechnicianProfile.builder()
                .user(user)
                .technicianStatus(TechnicianStatus.PENDING_KYC)
                .kycStatus(KycStatus.PENDING)
                .available(false)
                .experienceYears(request.getExperienceYears())
                .bio(request.getBio())
                .basePrice(request.getBasePrice())
                .hourlyRate(request.getHourlyRate())
                .reviewCount(0)
                .completedJobCount(0)
                .ratingAverage(java.math.BigDecimal.ZERO)
                .build();

        TechnicianProfile savedProfile = technicianProfileRepository.save(profile);

        log.info("Technician profile created for user: {} with ID: {}", userId, savedProfile.getId());

        return TechnicianRegisterResponse.builder()
                .technicianProfileId(savedProfile.getId())
                .technicianStatus(TechnicianStatus.PENDING_KYC)
                .kycStatus(KycStatus.PENDING)
                .nextStep("UPLOAD_KYC_DOCUMENTS")
                .build();
    }

    /**
     * Lấy danh sách thợ công khai (chỉ ACTIVE và APPROVED).
     */
    @Transactional(readOnly = true)
    public Page<TechnicianListItemResponse> getPublicTechnicians(
            String keyword,
            Long serviceId,
            String province,
            String district,
            Double minRating,
            Boolean available,
            Pageable pageable) {

        log.info("Fetching public technician list - available: {}, minRating: {}", available, minRating);

        // Nếu available = true, lấy thợ có sẵn sàng nhận đơn
        Page<TechnicianProfile> profiles;
        if (available != null && available) {
            profiles = technicianProfileRepository.findAvailableTechnicians(pageable);
        } else {
            profiles = technicianProfileRepository.findActiveAndVerifiedTechnicians(pageable);
        }

        return profiles.map(this::mapToListResponse);
    }

    /**
     * Lấy chi tiết hồ sơ thợ.
     * Nếu chưa ACTIVE/APPROVED thì chỉ owner/admin mới xem được.
     */
    @Transactional(readOnly = true)
    public TechnicianDetailResponse getTechnicianDetail(Long technicianId, Long currentUserId, boolean isAdmin) {
        log.info("Fetching technician detail - technicianId: {}", technicianId);

        TechnicianProfile profile = technicianProfileRepository.findById(technicianId)
                .orElseThrow(() -> new AppException("TECHNICIAN_NOT_FOUND", "Technician not found"));

        // Authorization check
        boolean isOwner = profile.getUser().getId().equals(currentUserId);
        boolean isPublic = profile.getTechnicianStatus() == TechnicianStatus.ACTIVE && 
                          profile.getKycStatus() == KycStatus.APPROVED;

        if (!isPublic && !isOwner && !isAdmin) {
            throw new AppException("UNAUTHORIZED", "Cannot view this technician profile");
        }

        return mapToDetailResponse(profile);
    }

    /**
     * Cập nhật hồ sơ thợ.
     */
    @Transactional
    public TechnicianDetailResponse updateTechnicianProfile(
            Long technicianId,
            TechnicianUpdateProfileRequest request,
            Long currentUserId) {

        log.info("Updating technician profile - technicianId: {}", technicianId);

        TechnicianProfile profile = technicianProfileRepository.findById(technicianId)
                .orElseThrow(() -> new AppException("TECHNICIAN_NOT_FOUND", "Technician not found"));

        // Authorization check - chỉ owner mới sửa
        if (!profile.getUser().getId().equals(currentUserId)) {
            throw new AppException("UNAUTHORIZED", "You can only update your own profile");
        }

        // Validate input
        if (request.getBasePrice() != null && request.getBasePrice() < 0) {
            throw new AppException("INVALID_PRICE", "Base price cannot be negative");
        }
        if (request.getHourlyRate() != null && request.getHourlyRate() < 0) {
            throw new AppException("INVALID_PRICE", "Hourly rate cannot be negative");
        }
        if (request.getExperienceYears() != null && request.getExperienceYears() < 0) {
            throw new AppException("INVALID_EXPERIENCE", "Experience years cannot be negative");
        }

        // Update fields
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getExperienceYears() != null) {
            profile.setExperienceYears(request.getExperienceYears());
        }
        if (request.getBasePrice() != null) {
            profile.setBasePrice(request.getBasePrice());
        }
        if (request.getHourlyRate() != null) {
            profile.setHourlyRate(request.getHourlyRate());
        }

        // Update user fields
        User user = profile.getUser();
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatar(request.getAvatarUrl());
        }

        userRepository.save(user);
        TechnicianProfile updated = technicianProfileRepository.save(profile);

        log.info("Technician profile updated - technicianId: {}", technicianId);

        return mapToDetailResponse(updated);
    }

    /**
     * Cập nhật trạng thái sẵn sàng nhận đơn (available).
     * Chỉ có thể bật nếu:
     * - kycStatus = APPROVED
     * - technicianStatus = ACTIVE
     * - Tài khoản không bị khóa
     */
    @Transactional
    public TechnicianAvailabilityResponse updateAvailability(
            Long technicianId,
            TechnicianAvailabilityRequest request,
            Long currentUserId) {

        log.info("Updating technician availability - technicianId: {}", technicianId);

        TechnicianProfile profile = technicianProfileRepository.findById(technicianId)
                .orElseThrow(() -> new AppException("TECHNICIAN_NOT_FOUND", "Technician not found"));

        // Authorization check
        if (!profile.getUser().getId().equals(currentUserId)) {
            throw new AppException("UNAUTHORIZED", "You can only update your own availability");
        }

        // Check if trying to enable availability
        if (request.getAvailable() != null && request.getAvailable()) {
            if (profile.getKycStatus() != KycStatus.APPROVED) {
                throw new AppException("KYC_NOT_APPROVED", 
                        "Technician account is not verified yet");
            }
            if (profile.getTechnicianStatus() != TechnicianStatus.ACTIVE) {
                throw new AppException("TECHNICIAN_NOT_ACTIVE", 
                        "Technician account is not active");
            }
            if (profile.getUser().isDeleted()) {
                throw new AppException("ACCOUNT_DELETED", 
                        "Your account is deleted or locked");
            }
        }

        profile.setAvailable(request.getAvailable());
        TechnicianProfile updated = technicianProfileRepository.save(profile);

        log.info("Technician availability updated to: {} - technicianId: {}", 
                request.getAvailable(), technicianId);

        return TechnicianAvailabilityResponse.builder()
                .technicianId(updated.getId())
                .available(updated.getAvailable())
                .kycStatus(updated.getKycStatus())
                .technicianStatus(updated.getTechnicianStatus())
                .message("Availability updated successfully")
                .build();
    }

    /**
     * Lấy danh sách review của thợ.
     * Chỉ lấy review từ booking đã completed.
     */
    @Transactional(readOnly = true)
    public Page<TechnicianReviewResponse> getTechnicianReviews(Long technicianId, Pageable pageable) {
        log.info("Fetching reviews for technician - technicianId: {}", technicianId);

        // Check technician exists
        TechnicianProfile profile = technicianProfileRepository.findById(technicianId)
                .orElseThrow(() -> new AppException("TECHNICIAN_NOT_FOUND", "Technician not found"));

        // Get reviews for this technician
        Page<com.example.becommerce.entity.Review> reviews = reviewRepository.findByTechnicianId(
                profile.getUser().getId(), pageable);

        return reviews.map(this::mapToReviewResponse);
    }

    /**
     * Map TechnicianProfile to list response
     */
    private TechnicianListItemResponse mapToListResponse(TechnicianProfile profile) {
        User user = profile.getUser();
        return TechnicianListItemResponse.builder()
                .technicianId(profile.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatar())
                .bio(profile.getBio())
                .services(new ArrayList<>()) // TODO: Fetch từ technician_skills table
                .ratingAverage(profile.getRatingAverage())
                .reviewCount(profile.getReviewCount())
                .completedJobCount(profile.getCompletedJobCount())
                .basePrice(profile.getBasePrice())
                .hourlyRate(profile.getHourlyRate())
                .available(profile.getAvailable())
                .workingAreas(new ArrayList<>()) // TODO: Fetch từ technician_service_areas table
                .build();
    }

    /**
     * Map TechnicianProfile to detail response
     */
    private TechnicianDetailResponse mapToDetailResponse(TechnicianProfile profile) {
        User user = profile.getUser();
        return TechnicianDetailResponse.builder()
                .technicianId(profile.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatar())
                .bio(profile.getBio())
                .experienceYears(profile.getExperienceYears())
                .services(new ArrayList<>()) // TODO: Fetch từ technician_skills table
                .workingAreas(new ArrayList<>()) // TODO: Fetch từ technician_service_areas table
                .ratingAverage(profile.getRatingAverage())
                .reviewCount(profile.getReviewCount())
                .completedJobCount(profile.getCompletedJobCount())
                .basePrice(profile.getBasePrice())
                .hourlyRate(profile.getHourlyRate())
                .available(profile.getAvailable())
                .technicianStatus(profile.getTechnicianStatus())
                .kycStatus(profile.getKycStatus())
                .createdAt(profile.getCreatedAt())
                .build();
    }

    /**
     * Map Review to response
     */
    private TechnicianReviewResponse mapToReviewResponse(com.example.becommerce.entity.Review review) {
        User customer = userRepository.findById(review.getCustomerId())
                .orElse(null);

        return TechnicianReviewResponse.builder()
                .reviewId(review.getId())
                .customerName(customer != null ? customer.getFullName() : "Unknown")
                .customerAvatar(customer != null ? customer.getAvatar() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .images(new ArrayList<>()) // TODO: Fetch từ review_images table
                .bookingId(review.getOrderId())
                .createdAt(review.getCreatedAt())
                .build();
    }

    /**
     * Get technician profile by user ID
     */
    @Transactional(readOnly = true)
    public TechnicianProfile getTechnicianProfileByUserId(Long userId) {
        return technicianProfileRepository.findByUserId(userId)
                .orElse(null);
    }
}
