package com.example.becommerce.service.impl;

import com.example.becommerce.dto.request.UploadVerificationDocumentRequest;
import com.example.becommerce.dto.request.VerificationSubmitRequest;
import com.example.becommerce.dto.request.VerificationUpdateStatusRequest;
import com.example.becommerce.dto.response.VerificationDocumentResponse;
import com.example.becommerce.dto.response.VerificationResponse;
import com.example.becommerce.entity.Verification;
import com.example.becommerce.entity.VerificationDocument;
import com.example.becommerce.entity.enums.KycStatus;
import com.example.becommerce.exception.ResourceNotFoundException;
import com.example.becommerce.exception.ValidationException;
import com.example.becommerce.repository.VerificationDocumentRepository;
import com.example.becommerce.repository.VerificationRepository;
import com.example.becommerce.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationRepository verificationRepository;
    private final VerificationDocumentRepository documentRepository;

    @Override
    @Transactional
    public VerificationResponse submitVerification(Long userId, VerificationSubmitRequest request) {
        // Check if already has pending verification
        verificationRepository.findTopByTechnicianIdOrderByCreatedAtDesc(userId)
                .ifPresent(v -> {
                    if (v.getStatus() == KycStatus.PENDING) {
                        throw new ValidationException("Verification already submitted. Waiting for admin review.");
                    }
                });

        // Create verification
        Verification verification = Verification.builder()
                .code(generateCode())
                .technicianId(userId)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .district(request.getDistrict())
                .city(request.getCity())
                .yearsExperience(request.getYearsExperience())
                .status(KycStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();

        Verification saved = verificationRepository.save(verification);
        return mapToResponse(saved);
    }

    @Override
    public VerificationResponse getVerification(Long verificationId, Long userId, boolean isAdmin) {
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found"));

        // Authorization
        if (!isAdmin && !verification.getTechnicianId().equals(userId)) {
            throw new ValidationException("Unauthorized");
        }

        return mapToResponse(verification);
    }

    @Override
    public VerificationResponse getTechnicianVerification(Long technicianId, Long userId, boolean isAdmin) {
        Verification verification = verificationRepository.findTopByTechnicianIdOrderByCreatedAtDesc(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("No verification found for this technician"));

        // Authorization
        if (!isAdmin && !technicianId.equals(userId)) {
            throw new ValidationException("Unauthorized");
        }

        return mapToResponse(verification);
    }

    @Override
    public Page<VerificationResponse> getVerificationsByStatus(String status, Pageable pageable) {
        KycStatus kycStatus = KycStatus.valueOf(status.toUpperCase());
        return verificationRepository.findByStatus(kycStatus, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public VerificationResponse updateVerificationStatus(Long verificationId, VerificationUpdateStatusRequest request, Long adminId) {
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found"));

        verification.setStatus(request.getStatus());
        verification.setNote(request.getNote());
        verification.setReviewedBy(adminId);
        verification.setReviewedAt(LocalDateTime.now());

        Verification updated = verificationRepository.save(verification);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public VerificationResponse uploadDocument(Long verificationId, UploadVerificationDocumentRequest request, Long userId) {
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found"));

        // Authorization
        if (!verification.getTechnicianId().equals(userId)) {
            throw new ValidationException("Unauthorized");
        }

        // Check if document already exists
        if (documentRepository.existsByVerificationIdAndDocType(verificationId, request.getDocType())) {
            throw new ValidationException("Document of this type already exists");
        }

        // Create document
        VerificationDocument document = VerificationDocument.builder()
                .verificationId(verificationId)
                .docType(request.getDocType())
                .url(request.getDocumentUrl())
                .build();

        documentRepository.save(document);
        return mapToResponse(verification);
    }

    @Override
    public boolean isKycApproved(Long technicianId) {
        return verificationRepository.findTopByTechnicianIdOrderByCreatedAtDesc(technicianId)
                .map(v -> v.getStatus() == KycStatus.APPROVED)
                .orElse(false);
    }

    // Helper
    private String generateCode() {
        return "VER-" + System.currentTimeMillis();
    }

    private VerificationResponse mapToResponse(Verification v) {
        var documents = documentRepository.findByVerificationId(v.getVerificationId())
                .stream()
                .map(d -> VerificationDocumentResponse.builder()
                        .documentId(d.getDocumentId())
                        .docType(d.getDocType())
                        .url(d.getUrl())
                        .build())
                .collect(Collectors.toList());

        return VerificationResponse.builder()
                .verificationId(v.getVerificationId())
                .code(v.getCode())
                .technicianId(v.getTechnicianId())
                .fullName(v.getFullName())
                .phone(v.getPhone())
                .email(v.getEmail())
                .district(v.getDistrict())
                .city(v.getCity())
                .yearsExperience(v.getYearsExperience())
                .status(v.getStatus())
                .note(v.getNote())
                .submittedAt(v.getSubmittedAt())
                .reviewedAt(v.getReviewedAt())
                .reviewedBy(v.getReviewedBy())
                .documents(documents)
                .build();
    }
}
