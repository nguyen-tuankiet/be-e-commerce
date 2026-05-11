package com.example.becommerce.controller;

import com.example.becommerce.dto.request.admin.AdminSettingsRequest;
import com.example.becommerce.dto.request.admin.CommissionUpdateRequest;
import com.example.becommerce.dto.request.admin.WalletAdjustRequest;
import com.example.becommerce.dto.request.category.CategoryStatusRequest;
import com.example.becommerce.dto.request.category.CategoryUpsertRequest;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.admin.AdminSettingsSavedResponse;
import com.example.becommerce.dto.response.admin.AdminStatsResponse;
import com.example.becommerce.dto.response.admin.AdminTransactionsResponse;
import com.example.becommerce.dto.response.admin.CommissionResponse;
import com.example.becommerce.dto.response.admin.RecentOrdersResponse;
import com.example.becommerce.dto.response.admin.RevenueStatsResponse;
import com.example.becommerce.dto.response.admin.ServiceDistributionResponse;
import com.example.becommerce.dto.response.admin.WalletAdjustResponse;
import com.example.becommerce.dto.response.admin.WithdrawApproveResponse;
import com.example.becommerce.dto.response.admin.WithdrawRequestsResponse;
import com.example.becommerce.dto.response.category.CategoryDeleteResponse;
import com.example.becommerce.dto.response.category.CategoryListResponse;
import com.example.becommerce.dto.response.category.CategoryResponse;
import com.example.becommerce.dto.response.upload.ImageUploadResponse;
import com.example.becommerce.dto.response.upload.ImagesUploadResponse;
import com.example.becommerce.service.AdminService;
import com.example.becommerce.service.CategoryService;
import com.example.becommerce.service.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GrowupApiControllerTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private AdminService adminService;

    @Mock
    private FileStorageService fileStorageService;

    private MockMvc categoryMvc;
    private MockMvc adminMvc;
    private MockMvc uploadMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        categoryMvc = MockMvcBuilders.standaloneSetup(new CategoryController(categoryService)).build();
        adminMvc = MockMvcBuilders.standaloneSetup(new AdminController(adminService)).build();
        uploadMvc = MockMvcBuilders.standaloneSetup(new FileUploadController(fileStorageService)).build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void categoryApisReturnExpectedContracts() throws Exception {
        CategoryResponse category = categoryResponse();
        when(categoryService.getCategories("active"))
                .thenReturn(CategoryListResponse.builder().items(List.of(category)).build());
        when(categoryService.createCategory(any(CategoryUpsertRequest.class))).thenReturn(category);
        when(categoryService.updateCategory(eq("CAT-001"), any(CategoryUpsertRequest.class))).thenReturn(category);
        when(categoryService.deleteCategory("CAT-001"))
                .thenReturn(CategoryDeleteResponse.builder().id("CAT-001").message("Xóa danh mục thành công").build());
        when(categoryService.updateStatus(eq("CAT-001"), any(CategoryStatusRequest.class))).thenReturn(category);

        categoryMvc.perform(get("/api/categories").param("status", "active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].id").value("CAT-001"));

        MockMultipartFile icon = new MockMultipartFile("icon", "icon.png", "image/png", "png".getBytes());
        categoryMvc.perform(multipart("/api/categories")
                        .file(icon)
                        .param("title", "Máy lạnh")
                        .param("description", "Vệ sinh, bơm gas")
                        .param("priority", "high")
                        .param("status", "active"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Máy lạnh"));

        categoryMvc.perform(multipart("/api/categories/{id}", "CAT-001")
                        .file(icon)
                        .param("title", "Máy lạnh")
                        .param("description", "Mô tả mới")
                        .param("priority", "normal")
                        .param("status", "active")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("CAT-001"));

        categoryMvc.perform(delete("/api/categories/{id}", "CAT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Xóa danh mục thành công"));

        categoryMvc.perform(patch("/api/categories/{id}/status", "CAT-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"inactive\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("active"));
    }

    @Test
    void adminDashboardApisReturnExpectedContracts() throws Exception {
        when(adminService.getStats()).thenReturn(AdminStatsResponse.builder()
                .totalRevenue(metric(3240000000L))
                .totalProfit(metric(486000000))
                .activeTechnicians(metric(1204))
                .ordersToday(metric(42))
                .build());
        when(adminService.getRevenueStats("7days")).thenReturn(RevenueStatsResponse.builder()
                .range("7days")
                .items(List.of(RevenueStatsResponse.Item.builder()
                        .label("Thứ 2")
                        .value(BigDecimal.valueOf(45))
                        .date(LocalDate.of(2026, 5, 1))
                        .build()))
                .build());
        when(adminService.getServiceDistribution()).thenReturn(ServiceDistributionResponse.builder()
                .items(List.of(ServiceDistributionResponse.Item.builder()
                        .name("Máy lạnh")
                        .percentage(BigDecimal.valueOf(40))
                        .color("#3b82f6")
                        .build()))
                .build());
        when(adminService.getRecentOrders(5)).thenReturn(RecentOrdersResponse.builder().items(List.of()).build());

        adminMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRevenue.value").value(3240000000L));
        adminMvc.perform(get("/api/admin/stats/revenue").param("range", "7days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.range").value("7days"));
        adminMvc.perform(get("/api/admin/stats/service-distribution"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].name").value("Máy lạnh"));
        adminMvc.perform(get("/api/admin/orders/recent").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    void adminFinanceAndSettingsApisReturnExpectedContracts() throws Exception {
        when(adminService.getTransactions("all", "2026-05-07", 1, 10)).thenReturn(AdminTransactionsResponse.builder()
                .totalBalance(BigDecimal.valueOf(1450000000))
                .items(List.of(AdminTransactionsResponse.Item.builder()
                        .id("#TXN-882910")
                        .time("14:20")
                        .date("2026-05-07")
                        .partner(AdminTransactionsResponse.Partner.builder().name("Thái Ngọc").area("HCMC, Quận 1").build())
                        .type("commission")
                        .amount(BigDecimal.valueOf(125000))
                        .status("done")
                        .build()))
                .pagination(PagedResponse.PaginationMeta.builder().page(1).limit(10).total(1).totalPages(1).build())
                .build());
        when(adminService.getWithdrawRequests()).thenReturn(WithdrawRequestsResponse.builder()
                .pendingCount(1)
                .items(List.of(WithdrawRequestsResponse.Item.builder()
                        .id("WR-001")
                        .technician(WithdrawRequestsResponse.Technician.builder().id("TECH-001").fullName("Trần Anh Tuấn").avatar("TA").build())
                        .amount(BigDecimal.valueOf(2500000))
                        .bankName("Vietcombank")
                        .accountNumber("0071901232***")
                        .requestedAt(LocalDateTime.of(2026, 5, 7, 9, 0))
                        .status("pending")
                        .build()))
                .build());
        when(adminService.approveWithdrawRequest("WR-001")).thenReturn(WithdrawApproveResponse.builder()
                .id("WR-001")
                .status("approved")
                .processedAt(LocalDateTime.of(2026, 5, 7, 10, 0))
                .processedBy("Admin AD-9902")
                .build());
        when(adminService.updateCommission(any(CommissionUpdateRequest.class))).thenReturn(CommissionResponse.builder()
                .platformFeePercent(BigDecimal.valueOf(15))
                .vatPercent(BigDecimal.valueOf(10))
                .updatedBy("Admin AD-9902")
                .updatedAt(LocalDateTime.of(2026, 5, 7, 10, 0))
                .build());
        when(adminService.adjustWallet(any(WalletAdjustRequest.class))).thenReturn(WalletAdjustResponse.builder()
                .transactionId("TX-ADJ-001")
                .technicianId("TECH-001")
                .amount(BigDecimal.valueOf(-50000))
                .newBalance(BigDecimal.valueOf(1450000))
                .reason("Điều chỉnh hoa hồng đơn GU-99210")
                .createdAt(LocalDateTime.of(2026, 5, 7, 10, 0))
                .build());
        when(adminService.getSettings()).thenReturn(settingsRequest(false));
        when(adminService.updateSettings(any(AdminSettingsRequest.class))).thenReturn(AdminSettingsSavedResponse.builder()
                .message("Cấu hình đã được lưu")
                .updatedAt(LocalDateTime.of(2026, 5, 7, 10, 0))
                .build());

        adminMvc.perform(get("/api/admin/transactions")
                        .param("type", "all")
                        .param("date", "2026-05-07")
                        .param("page", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value("#TXN-882910"));
        adminMvc.perform(get("/api/admin/withdraw-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pendingCount").value(1));
        adminMvc.perform(post("/api/admin/withdraw-requests/{id}/approve", "WR-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("approved"));
        adminMvc.perform(patch("/api/admin/commission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"platformFeePercent\":15,\"vatPercent\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.platformFeePercent").value(15));
        adminMvc.perform(post("/api/admin/wallet/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"technicianId":"TECH-001","amount":-50000,"type":"commission-minus","reason":"Điều chỉnh hoa hồng đơn GU-99210"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newBalance").value(1450000));
        adminMvc.perform(get("/api/admin/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.general.appName").value("GlowUp Concierge"));
        adminMvc.perform(put("/api/admin/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settingsRequest(true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Cấu hình đã được lưu"));
    }

    @Test
    void fileUploadApisReturnExpectedContracts() throws Exception {
        MockMultipartFile image = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "jpg".getBytes());
        MockMultipartFile imageOne = new MockMultipartFile("files", "one.jpg", "image/jpeg", "one".getBytes());
        MockMultipartFile imageTwo = new MockMultipartFile("files", "two.jpg", "image/jpeg", "two".getBytes());

        when(fileStorageService.storeImage(any(), eq("avatars"))).thenReturn(ImageUploadResponse.builder()
                .url("/uploads/avatars/abc123.jpg")
                .filename("abc123.jpg")
                .size(245678)
                .mimeType("image/jpeg")
                .build());
        when(fileStorageService.storeImages(anyList(), eq("orders"))).thenReturn(ImagesUploadResponse.builder()
                .urls(List.of("/uploads/orders/img1.jpg", "/uploads/orders/img2.jpg"))
                .build());

        uploadMvc.perform(multipart("/api/upload/image").file(image).param("folder", "avatars"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.filename").value("abc123.jpg"));

        uploadMvc.perform(multipart("/api/upload/images").file(imageOne).file(imageTwo).param("folder", "orders"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.urls[0]").value("/uploads/orders/img1.jpg"));

        verify(fileStorageService).storeImages(anyList(), anyString());
    }

    private CategoryResponse categoryResponse() {
        return CategoryResponse.builder()
                .id("CAT-001")
                .title("Máy lạnh")
                .description("Vệ sinh, bơm gas")
                .iconUrl("/uploads/categories/icon.png")
                .priority("high")
                .status("active")
                .createdAt(LocalDateTime.of(2026, 5, 7, 10, 0))
                .build();
    }

    private AdminStatsResponse.Metric metric(long value) {
        return AdminStatsResponse.Metric.builder()
                .value(BigDecimal.valueOf(value))
                .change(BigDecimal.ZERO)
                .changeDirection("neutral")
                .build();
    }

    private AdminSettingsRequest settingsRequest(boolean weeklyRevenue) {
        AdminSettingsRequest request = new AdminSettingsRequest();
        AdminSettingsRequest.General general = new AdminSettingsRequest.General();
        general.setAppName("GlowUp Concierge");
        general.setHotline("1900 8888");
        general.setEmail("admin@glowup.vn");
        request.setGeneral(general);

        AdminSettingsRequest.Billing billing = new AdminSettingsRequest.Billing();
        billing.setPlatformFeePercent(BigDecimal.valueOf(15));
        billing.setVatPercent(BigDecimal.valueOf(10));
        request.setBilling(billing);

        AdminSettingsRequest.Notifications notifications = new AdminSettingsRequest.Notifications();
        notifications.setNewOrder(true);
        notifications.setCustomerEmail(true);
        notifications.setWeeklyRevenue(weeklyRevenue);
        notifications.setSecurityAlert(true);
        request.setNotifications(notifications);

        AdminSettingsRequest.Operations operations = new AdminSettingsRequest.Operations();
        operations.setRequireManualReview(true);
        operations.setTechnicianAutoPause(false);
        operations.setIncidentEscalation(true);
        request.setOperations(operations);
        return request;
    }
}
