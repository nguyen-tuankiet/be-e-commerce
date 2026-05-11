package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.verification.VerificationCreatedResponse;
import com.example.becommerce.dto.response.verification.VerificationDetailResponse;
import com.example.becommerce.dto.response.verification.VerificationDocumentsView;
import com.example.becommerce.dto.response.verification.VerificationListItemResponse;
import com.example.becommerce.dto.response.verification.VerificationReviewResponse;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.Verification;
import com.example.becommerce.entity.enums.VerificationStatus;
import org.springframework.stereotype.Component;

@Component
public class VerificationMapper {

    public VerificationListItemResponse toListItem(Verification v) {
        if (v == null) return null;
        User tech = v.getTechnician();

        return VerificationListItemResponse.builder()
                .id(v.getCode())
                .technicianId(tech == null ? null : tech.getCode())
                .fullName(tech == null ? null : tech.getFullName())
                .phone(tech == null ? null : tech.getPhone())
                .email(tech == null ? null : tech.getEmail())
                .district(v.getDistrict())
                .serviceCategory(v.getServiceCategory())
                .yearsExperience(v.getYearsExperience())
                .status(v.getStatus() == null ? null : v.getStatus().apiValue())
                .submittedAt(v.getSubmittedAt())
                .documents(toDocuments(v))
                .build();
    }

    public VerificationDetailResponse toDetail(Verification v) {
        if (v == null) return null;
        User tech = v.getTechnician();

        return VerificationDetailResponse.builder()
                .id(v.getCode())
                .technicianId(tech == null ? null : tech.getCode())
                .fullName(tech == null ? null : tech.getFullName())
                .phone(tech == null ? null : tech.getPhone())
                .email(tech == null ? null : tech.getEmail())
                .district(v.getDistrict())
                .serviceCategory(v.getServiceCategory())
                .yearsExperience(v.getYearsExperience())
                .status(v.getStatus() == null ? null : v.getStatus().apiValue())
                .note(v.getNote())
                .reviewedBy(v.getReviewedBy())
                .reviewedAt(v.getReviewedAt())
                .submittedAt(v.getSubmittedAt())
                .documents(toDocuments(v))
                .build();
    }

    public VerificationCreatedResponse toCreatedResponse(Verification v) {
        return VerificationCreatedResponse.builder()
                .id(v.getCode())
                .technicianId(v.getTechnician() == null ? null : v.getTechnician().getCode())
                .status(v.getStatus() == null ? null : v.getStatus().apiValue())
                .submittedAt(v.getSubmittedAt())
                .build();
    }

    public VerificationReviewResponse toReviewResponse(Verification v, VerificationStatus technicianStatus) {
        return VerificationReviewResponse.builder()
                .id(v.getCode())
                .status(v.getStatus() == null ? null : v.getStatus().apiValue())
                .note(v.getNote())
                .reviewedBy(v.getReviewedBy())
                .reviewedAt(v.getReviewedAt())
                .technicianStatus(technicianStatus == null ? null : technicianStatus.apiValue())
                .build();
    }

    private VerificationDocumentsView toDocuments(Verification v) {
        if (v.getIdFront() == null && v.getIdBack() == null
                && v.getPortrait() == null && v.getCertificate() == null) {
            return null;
        }
        return VerificationDocumentsView.builder()
                .idFront(v.getIdFront())
                .idBack(v.getIdBack())
                .portrait(v.getPortrait())
                .certificate(v.getCertificate())
                .build();
    }
}
