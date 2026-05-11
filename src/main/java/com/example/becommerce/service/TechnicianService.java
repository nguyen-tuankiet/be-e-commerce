package com.example.becommerce.service;

import com.example.becommerce.dto.request.technician.UpdateAvailabilityRequest;
import com.example.becommerce.dto.request.technician.UpdateTechnicianProfileRequest;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.technician.AvailabilityResponse;
import com.example.becommerce.dto.response.technician.TechnicianDetailResponse;
import com.example.becommerce.dto.response.technician.TechnicianListItemResponse;
import com.example.becommerce.dto.response.technician.TechnicianProfileUpdateResponse;
import com.example.becommerce.dto.response.technician.TechnicianReviewListResponse;

public interface TechnicianService {

    PagedResponse<TechnicianListItemResponse> listTechnicians(
            String service, String district, Boolean isAvailable, Double minRating,
            String keyword, int page, int limit);

    TechnicianDetailResponse getTechnician(String code);

    TechnicianProfileUpdateResponse updateProfile(String code, UpdateTechnicianProfileRequest request);

    AvailabilityResponse updateAvailability(String code, UpdateAvailabilityRequest request);

    TechnicianReviewListResponse listReviews(String code, int page, int limit);
}
