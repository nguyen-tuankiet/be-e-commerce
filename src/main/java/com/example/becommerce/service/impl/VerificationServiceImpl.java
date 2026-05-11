package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.VerificationMapper;
import com.example.becommerce.dto.request.verification.CreateVerificationRequest;
import com.example.becommerce.dto.request.verification.ReviewVerificationRequest;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.verification.VerificationCreatedResponse;
import com.example.becommerce.dto.response.verification.VerificationDetailResponse;
import com.example.becommerce.dto.response.verification.VerificationListItemResponse;
import com.example.becommerce.dto.response.verification.VerificationReviewResponse;
import com.example.becommerce.entity.TechnicianProfile;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.Verification;
import com.example.becommerce.entity.enums.Role;
import com.example.becommerce.entity.enums.VerificationStatus;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.TechnicianProfileRepository;
import com.example.becommerce.repository.UserRepository;
import com.example.becommerce.repository.VerificationRepository;
import com.example.becommerce.service.FileStorageService;
import com.example.becommerce.service.VerificationService;
import com.example.becommerce.utils.VerificationCodeGenerator;
import com.example.becommerce.utils.VerificationSpecification;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Verification (KYC) business operations.
 *
 * <p>Workflow
 *  <ol>
 *    <li>Technician submits documents (multipart upload).</li>
 *    <li>Admin reviews and approves/rejects.</li>
 *    <li>On approval the technician's {@link TechnicianProfile#getVerificationStatus()}
 *        is mirrored to APPROVED so it surfaces in the technician detail view.</li>
 *  </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationServiceImpl implements VerificationService {

    private static final String UPLOAD_FOLDER = "verifications";

    private final VerificationRepository      verificationRepository;
    private final UserRepository              userRepository;
    private final TechnicianProfileRepository profileRepository;
    private final FileStorageService          fileStorageService;
    private final VerificationMapper          verificationMapper;
    private final VerificationCodeGenerator   codeGenerator;

    // ===============================================================
    // LIST (admin)
    // ===============================================================

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VerificationListItemResponse> list(
            String status, String keyword, int page, int limit) {

        Specification<Verification> spec = VerificationSpecification.buildFilter(status, keyword);

        Pageable pageable = PageRequest.of(
                Math.max(0, page - 1),
                limit,
                Sort.by(Sort.Direction.DESC, "submittedAt"));

        Page<Verification> resultPage = verificationRepository.findAll(spec, pageable);

        List<VerificationListItemResponse> items = resultPage.getContent().stream()
                .map(verificationMapper::toListItem)
                .toList();

        return PagedResponse.of(items, page, limit, resultPage.getTotalElements());
    }

    // ===============================================================
    // SUBMIT (technician)
    // ===============================================================

    @Override
    @Transactional
    public VerificationCreatedResponse submit(CreateVerificationRequest request) {
        User current = getCurrentUser();
        if (current.getRole() != Role.TECHNICIAN) {
            throw AppException.forbidden("Chỉ thợ mới có thể gửi hồ sơ xác minh");
        }

        // Optional override allowed only for admin (CSR convenience).
        User technician = current;
        if (request.getTechnicianId() != null && !request.getTechnicianId().equals(current.getCode())) {
            if (current.getRole() != Role.ADMIN) {
                throw AppException.forbidden("Không thể gửi hồ sơ thay cho người khác");
            }
            technician = userRepository.findByCodeAndDeletedFalse(request.getTechnicianId())
                    .orElseThrow(() -> AppException.notFound("Không tìm thấy thợ " + request.getTechnicianId()));
        }

        if (verificationRepository.existsByTechnician_IdAndStatus(technician.getId(), VerificationStatus.PENDING)) {
            throw AppException.conflict(ErrorCode.VERIFICATION_PENDING_EXISTS,
                    "Đang có hồ sơ chờ duyệt cho thợ này, không thể nộp thêm");
        }

        Verification verification = Verification.builder()
                .code(codeGenerator.generate())
                .technician(technician)
                .district(request.getDistrict())
                .serviceCategory(request.getServiceCategory())
                .yearsExperience(request.getYearsExperience())
                .idFront(storeOptional(request.getIdFront()))
                .idBack(storeOptional(request.getIdBack()))
                .portrait(storeOptional(request.getPortrait()))
                .certificate(storeOptional(request.getCertificate()))
                .status(VerificationStatus.PENDING)
                .build();

        Verification saved = verificationRepository.save(verification);

        // Sync profile.verificationStatus = PENDING
        TechnicianProfile profile = profileRepository.findByUser_Id(technician.getId())
                .orElseGet(() -> TechnicianProfile.builder().user(saved.getTechnician()).build());
        profile.setVerificationStatus(VerificationStatus.PENDING);
        profileRepository.save(profile);

        log.info("Verification {} submitted by technician {}", saved.getCode(), technician.getCode());
        return verificationMapper.toCreatedResponse(saved);
    }

    // ===============================================================
    // GET DETAIL (admin or owner)
    // ===============================================================

    @Override
    @Transactional(readOnly = true)
    public VerificationDetailResponse get(String code) {
        Verification v = findByCode(code);
        ensureCanRead(v);
        return verificationMapper.toDetail(v);
    }

    // ===============================================================
    // REVIEW (admin approve/reject)
    // ===============================================================

    @Override
    @Transactional
    public VerificationReviewResponse review(String code, ReviewVerificationRequest request) {
        User admin = getCurrentUser();
        if (admin.getRole() != Role.ADMIN) {
            throw AppException.forbidden("Chỉ admin mới có thể duyệt hồ sơ");
        }

        Verification v = findByCode(code);

        VerificationStatus target;
        try {
            target = VerificationStatus.from(request.getStatus());
        } catch (IllegalArgumentException ex) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Trạng thái duyệt không hợp lệ");
        }
        if (target != VerificationStatus.APPROVED && target != VerificationStatus.REJECTED) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR,
                    "Chỉ chấp nhận status approved hoặc rejected");
        }

        v.setStatus(target);
        v.setNote(request.getNote());
        v.setReviewedBy(request.getReviewedBy() != null ? request.getReviewedBy() : admin.getCode());
        v.setReviewedAt(LocalDateTime.now());
        Verification saved = verificationRepository.save(v);

        // Sync profile.verificationStatus
        VerificationStatus syncedStatus = target;
        TechnicianProfile profile = profileRepository.findByUser_Id(v.getTechnician().getId())
                .orElseGet(() -> TechnicianProfile.builder().user(v.getTechnician()).build());
        profile.setVerificationStatus(target);
        profileRepository.save(profile);

        if (Boolean.TRUE.equals(request.getNotifyTechnician())) {
            // Wire up to NotificationService when ready — left as no-op to keep
            // this module independent of notification implementation details.
            log.info("[notify] verification {} -> technician {}: status={}",
                    saved.getCode(), v.getTechnician().getCode(), target);
        }

        return verificationMapper.toReviewResponse(saved, syncedStatus);
    }

    // ===============================================================
    // Helpers
    // ===============================================================

    private void ensureCanRead(Verification v) {
        User current = getCurrentUser();
        if (current.getRole() == Role.ADMIN) return;
        if (current.getId().equals(v.getTechnician().getId())) return;
        throw AppException.forbidden("Bạn không có quyền xem hồ sơ này");
    }

    private Verification findByCode(String code) {
        return verificationRepository.findByCode(code)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy hồ sơ xác minh " + code));
    }

    private String storeOptional(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        return fileStorageService.storeImage(file, UPLOAD_FOLDER).getUrl();
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
