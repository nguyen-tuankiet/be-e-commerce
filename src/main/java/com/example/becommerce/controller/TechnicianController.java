package com.example.becommerce.controller;

import com.example.becommerce.dto.request.technician.UpdateAvailabilityRequest;
import com.example.becommerce.dto.request.technician.UpdateTechnicianProfileRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.technician.*;
import com.example.becommerce.service.OrderService;
import com.example.becommerce.service.TechnicianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
@Validated
public class TechnicianController {

    private final TechnicianService technicianService;
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TechnicianListItemResponse>>> list(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                technicianService.listTechnicians(service, district, isAvailable, minRating, keyword, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TechnicianDetailResponse>> get(@PathVariable("id") String code) {
        return ResponseEntity.ok(ApiResponse.success(technicianService.getTechnician(code)));
    }

    @PatchMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<TechnicianProfileUpdateResponse>> updateProfile(
            @PathVariable("id") String code,
            @Valid @RequestBody UpdateTechnicianProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(technicianService.updateProfile(code, request)));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> updateAvailability(
            @PathVariable("id") String code,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.success(technicianService.updateAvailability(code, request)));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<TechnicianReviewListResponse>> listReviews(
            @PathVariable("id") String code,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.success(technicianService.listReviews(code, page, limit)));
    }

    @GetMapping("/{id}/busy-slots")
    public ResponseEntity<ApiResponse<List<BusySlotResponse>>> getTechnicianBusySlots(@PathVariable String id) {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.getTechnicianBusySlots(id))
        );
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(orderService.getDashboardStats()));
    }

    @GetMapping("/dashboard/chart")
    public ResponseEntity<ApiResponse<List<ChartDataResponse>>> getChart(@RequestParam(defaultValue = "week") String period) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getEarningsChart(period)));
    }
}
