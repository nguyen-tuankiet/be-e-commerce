package com.example.becommerce.controller;

import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.AuthResponse;
import com.example.becommerce.dto.response.BankAccountDeleteResponse;
import com.example.becommerce.dto.response.BankAccountListResponse;
import com.example.becommerce.dto.response.BankAccountResponse;
import com.example.becommerce.dto.response.MarkReadResponse;
import com.example.becommerce.dto.response.NotificationListResponse;
import com.example.becommerce.dto.response.NotificationResponse;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.ReadAllNotificationResponse;
import com.example.becommerce.dto.response.TokenRefreshResponse;
import com.example.becommerce.dto.response.UserResponse;
import com.example.becommerce.dto.response.VnpayIpnResponse;
import com.example.becommerce.dto.response.WalletResponse;
import com.example.becommerce.dto.response.WalletTopUpConfirmResponse;
import com.example.becommerce.dto.response.WalletTopUpResponse;
import com.example.becommerce.dto.response.WalletTransactionResponse;
import com.example.becommerce.dto.response.WalletWithdrawResponse;
import com.example.becommerce.dto.request.order.CancelOrderRequest;
import com.example.becommerce.dto.request.order.CompleteOrderRequest;
import com.example.becommerce.dto.request.order.CreateOrderRequest;
import com.example.becommerce.dto.request.order.PriceAdjustmentRequest;
import com.example.becommerce.dto.request.order.RejectOrderRequest;
import com.example.becommerce.dto.request.order.RejectPriceAdjustmentRequest;
import com.example.becommerce.dto.request.order.UpdateOrderStatusRequest;
import com.example.becommerce.dto.response.order.OrderResponse;
import com.example.becommerce.dto.response.order.OrderStatusChangeResponse;
import com.example.becommerce.dto.response.order.PriceAdjustmentEnvelope;
import com.example.becommerce.dto.response.chat.ConversationCreatedResponse;
import com.example.becommerce.dto.response.chat.ConversationListItemResponse;
import com.example.becommerce.dto.response.chat.MessageResponse;
import com.example.becommerce.dto.response.quotation.AcceptQuotationResponse;
import com.example.becommerce.dto.response.quotation.QuotationResponse;
import com.example.becommerce.dto.response.report.ReportResponse;
import com.example.becommerce.dto.response.review.ReviewResponse;
import com.example.becommerce.dto.response.technician.AvailabilityResponse;
import com.example.becommerce.dto.response.technician.TechnicianDetailResponse;
import com.example.becommerce.dto.response.technician.TechnicianListItemResponse;
import com.example.becommerce.dto.response.technician.TechnicianProfileUpdateResponse;
import com.example.becommerce.dto.response.technician.TechnicianReviewListResponse;
import com.example.becommerce.dto.response.verification.VerificationCreatedResponse;
import com.example.becommerce.dto.response.verification.VerificationDetailResponse;
import com.example.becommerce.dto.response.verification.VerificationListItemResponse;
import com.example.becommerce.dto.response.verification.VerificationReviewResponse;
import com.example.becommerce.dto.response.warranty.WarrantyResponse;
import com.example.becommerce.exception.GlobalExceptionHandler;
import com.example.becommerce.service.AuthService;
import com.example.becommerce.service.ConversationService;
import com.example.becommerce.service.NotificationService;
import com.example.becommerce.service.OrderService;
import com.example.becommerce.service.QuotationService;
import com.example.becommerce.service.ReportService;
import com.example.becommerce.service.ReviewService;
import com.example.becommerce.service.TechnicianService;
import com.example.becommerce.service.UserService;
import com.example.becommerce.service.VerificationService;
import com.example.becommerce.service.WalletService;
import com.example.becommerce.service.WarrantyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API contract tests (controller layer) for the modules NOT covered by
 * {@link GrowupApiControllerTest}: auth, users, wallet, orders, technicians,
 * verifications, conversations, quotes, reports, notifications, order reports,
 * order reviews, order warranty and the VNPay payment webhook.
 *
 * <p>Strategy — {@code MockMvcBuilders.standaloneSetup} with mocked services
 * plus the real {@link GlobalExceptionHandler} so happy-path status codes,
 * the {@code success}/{@code data} response wrapper, route + HTTP method
 * binding and JSR-303 body validation (HTTP 422) are all exercised without a
 * database or full Spring context.</p>
 */
@ExtendWith(MockitoExtension.class)
class SystemApiControllerTest {

    @Mock private AuthService authService;
    @Mock private UserService userService;
    @Mock private WalletService walletService;
    @Mock private OrderService orderService;
    @Mock private TechnicianService technicianService;
    @Mock private VerificationService verificationService;
    @Mock private ConversationService conversationService;
    @Mock private QuotationService quotationService;
    @Mock private ReportService reportService;
    @Mock private ReviewService reviewService;
    @Mock private WarrantyService warrantyService;
    @Mock private NotificationService notificationService;

    private MockMvc authMvc;
    private MockMvc userMvc;
    private MockMvc walletMvc;
    private MockMvc orderMvc;
    private MockMvc technicianMvc;
    private MockMvc verificationMvc;
    private MockMvc conversationMvc;
    private MockMvc quotationMvc;
    private MockMvc reportMvc;
    private MockMvc orderReportMvc;
    private MockMvc orderReviewMvc;
    private MockMvc orderWarrantyMvc;
    private MockMvc notificationMvc;
    private MockMvc paymentMvc;

    @BeforeEach
    void setUp() {
        GlobalExceptionHandler advice = new GlobalExceptionHandler();
        authMvc         = std(new AuthController(authService), advice);
        userMvc         = std(new UserController(userService), advice);
        walletMvc       = std(new WalletController(walletService), advice);
        orderMvc        = std(new OrderController(orderService), advice);
        technicianMvc   = std(new TechnicianController(technicianService), advice);
        verificationMvc = std(new VerificationController(verificationService), advice);
        conversationMvc = std(new ConversationController(conversationService, quotationService), advice);
        quotationMvc    = std(new QuotationController(quotationService), advice);
        reportMvc       = std(new ReportController(reportService), advice);
        orderReportMvc  = std(new OrderReportController(reportService), advice);
        orderReviewMvc  = std(new OrderReviewController(reviewService), advice);
        orderWarrantyMvc= std(new OrderWarrantyController(warrantyService), advice);
        notificationMvc = std(new NotificationController(notificationService), advice);
        paymentMvc      = std(new PaymentWebhookController(walletService), advice);
    }

    private static MockMvc std(Object controller, GlobalExceptionHandler advice) {
        return MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(advice).build();
    }

    // ================================================================
    // AUTH  /api/auth
    // ================================================================

    @Test
    @DisplayName("AUTH — register / login / refresh / logout / me / forgot / change / verify")
    void authApisReturnExpectedContracts() throws Exception {
        UserResponse user = sampleUser();
        AuthResponse auth = AuthResponse.builder()
                .accessToken("access.jwt.token")
                .refreshToken("refresh.jwt.token")
                .user(user)
                .build();

        when(authService.register(any())).thenReturn(auth);
        when(authService.login(any())).thenReturn(auth);
        when(authService.refreshToken(any()))
                .thenReturn(TokenRefreshResponse.builder().accessToken("new.access.token").build());
        when(authService.getCurrentUser(anyString())).thenReturn(user);

        // register — 201
        authMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"fullName":"Nguyễn Văn A","email":"a@example.com","phone":"0901234567",
                 "password":"Abc@1234","role":"customer"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access.jwt.token"));

        // login — 200
        authMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"identifier":"a@example.com","password":"Abc@1234"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.email").value("a@example.com"));

        // refresh-token — 200
        authMvc.perform(post("/api/auth/refresh-token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh.jwt.token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new.access.token"));

        // logout — 200
        authMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer x"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // forgot-password — 200
        authMvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"a@example.com\"}"))
                .andExpect(status().isOk());

        // change-password — 200
        authMvc.perform(post("/api/auth/change-password").contentType(MediaType.APPLICATION_JSON).content("""
                {"newPassword":"Xyz@5678","confirmPassword":"Xyz@5678","token":"reset-token"}"""))
                .andExpect(status().isOk());

        // me — 200
        authMvc.perform(get("/api/auth/me").header("Authorization", "Bearer x"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("US-001"));

        // verify-email — 200
        authMvc.perform(post("/api/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"verify-token\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AUTH (negative) — invalid email / weak password / bad role / blank login → 422")
    void authValidationErrors() throws Exception {
        // bad email + weak password + invalid role
        authMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"fullName":"A","email":"not-an-email","phone":"123","password":"weak","role":"admin"}"""))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // blank login fields
        authMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"\",\"password\":\"\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ================================================================
    // USERS  /api/users
    // ================================================================

    @Test
    @DisplayName("USERS — list / detail / update profile / update status")
    void userApisReturnExpectedContracts() throws Exception {
        UserResponse user = sampleUser();
        when(userService.getUsers(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(PagedResponse.of(List.of(user), 1, 10, 1));
        when(userService.getUserById(1L)).thenReturn(user);
        when(userService.updateUser(eq(1L), any())).thenReturn(user);
        when(userService.updateUserStatus(eq(1L), any())).thenReturn(user);

        userMvc.perform(get("/api/users").param("role", "customer").param("page", "1").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].code").value("US-001"))
                .andExpect(jsonPath("$.data.pagination.total").value(1));

        userMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        userMvc.perform(patch("/api/users/{id}", 1L).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Tên mới\",\"district\":\"Quận 1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        userMvc.perform(patch("/api/users/{id}/status", 1L).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"suspended\",\"reason\":\"Vi phạm\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("USERS (negative) — bad email on update / blank status → 422")
    void userValidationErrors() throws Exception {
        userMvc.perform(patch("/api/users/{id}", 1L).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bad\",\"phone\":\"123\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));

        userMvc.perform(patch("/api/users/{id}/status", 1L).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ================================================================
    // WALLET  /api/wallet
    // ================================================================

    @Test
    @DisplayName("WALLET — wallet / transactions / topup / confirm / withdraw / bank accounts")
    void walletApisReturnExpectedContracts() throws Exception {
        when(walletService.getCurrentWallet()).thenReturn(WalletResponse.builder()
                .userId("US-001").balance(BigDecimal.valueOf(1500000)).currency("VND").build());
        when(walletService.getTransactions(anyString(), anyInt(), anyInt()))
                .thenReturn(PagedResponse.of(List.of(WalletTransactionResponse.builder()
                        .id("TXN-001").type("credit").amount(BigDecimal.valueOf(100000)).status("done").build()),
                        1, 10, 1));
        when(walletService.topUp(any())).thenReturn(WalletTopUpResponse.builder()
                .transactionId("TOPUP-001").amount(BigDecimal.valueOf(100000)).method("vnpay")
                .checkoutUrl("https://sandbox.vnpayment.vn/pay").status("pending").build());
        when(walletService.confirmTopUp(any())).thenReturn(WalletTopUpConfirmResponse.builder()
                .transactionId("TOPUP-001").status("done").message("Nạp tiền thành công").build());
        when(walletService.withdraw(any())).thenReturn(WalletWithdrawResponse.builder()
                .transactionId("WD-001").amount(BigDecimal.valueOf(100000)).fee(BigDecimal.valueOf(5000))
                .netAmount(BigDecimal.valueOf(95000)).status("pending").build());
        when(walletService.getBankAccounts()).thenReturn(BankAccountListResponse.builder()
                .items(List.of(bankAccount())).build());
        when(walletService.createBankAccount(any())).thenReturn(bankAccount());
        when(walletService.deleteBankAccount("BA-001"))
                .thenReturn(BankAccountDeleteResponse.builder().message("Đã xóa tài khoản ngân hàng").build());

        walletMvc.perform(get("/api/wallet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(1500000));

        walletMvc.perform(get("/api/wallet/transactions").param("type", "all").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value("TXN-001"));

        walletMvc.perform(post("/api/wallet/topup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100000,\"method\":\"vnpay\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.transactionId").value("TOPUP-001"));

        walletMvc.perform(post("/api/wallet/topup/confirm").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transactionId\":\"TOPUP-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("done"));

        walletMvc.perform(post("/api/wallet/withdraw").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100000,\"bankAccountId\":\"BA-001\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.netAmount").value(95000));

        walletMvc.perform(get("/api/wallet/bank-accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].bankName").value("Vietcombank"));

        walletMvc.perform(post("/api/wallet/bank-accounts").contentType(MediaType.APPLICATION_JSON).content("""
                {"bankName":"Vietcombank","accountNumber":"0123456789","accountOwner":"NGUYEN VAN A"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("BA-001"));

        walletMvc.perform(delete("/api/wallet/bank-accounts/{id}", "BA-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Đã xóa tài khoản ngân hàng"));
    }

    @Test
    @DisplayName("WALLET (negative) — topup below min / bad method / withdraw below min → 422")
    void walletValidationErrors() throws Exception {
        walletMvc.perform(post("/api/wallet/topup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":5000,\"method\":\"bitcoin\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));

        walletMvc.perform(post("/api/wallet/withdraw").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1000,\"bankAccountId\":\"\"}"))
                .andExpect(status().isUnprocessableEntity());

        walletMvc.perform(post("/api/wallet/bank-accounts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bankName\":\"\",\"accountNumber\":\"abc\",\"accountOwner\":\"nguyen van a\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ================================================================
    // ORDERS  /api/orders
    // ================================================================

    @Test
    @DisplayName("ORDERS — list / detail / create / status / cancel / accept / reject / complete / price flow")
    void orderApisReturnExpectedContracts() throws Exception {
        OrderResponse order = OrderResponse.builder().id("GU-0001").status("pending")
                .serviceName("Sửa máy lạnh").deviceName("Máy lạnh Daikin").estimatedPrice(300000L).build();
        OrderStatusChangeResponse change = OrderStatusChangeResponse.builder()
                .id("GU-0001").status("accepted").message("Đã cập nhật").build();
        PriceAdjustmentEnvelope envelope = PriceAdjustmentEnvelope.builder().id("GU-0001").build();

        when(orderService.getOrders(any(), any(), anyInt(), anyInt()))
                .thenReturn(PagedResponse.of(List.of(order), 1, 10, 1));
        when(orderService.getOrderById("GU-0001")).thenReturn(order);
        when(orderService.createOrder(any())).thenReturn(order);
        when(orderService.updateStatus(eq("GU-0001"), any())).thenReturn(change);
        when(orderService.cancelOrder(eq("GU-0001"), any())).thenReturn(change);
        when(orderService.acceptOrder("GU-0001")).thenReturn(change);
        when(orderService.rejectOrder(eq("GU-0001"), any())).thenReturn(change);
        when(orderService.completeOrder(eq("GU-0001"), any())).thenReturn(change);
        when(orderService.requestPriceAdjustment(eq("GU-0001"), any())).thenReturn(envelope);
        when(orderService.approvePriceAdjustment("GU-0001")).thenReturn(envelope);
        when(orderService.rejectPriceAdjustment(eq("GU-0001"), any())).thenReturn(envelope);

        orderMvc.perform(get("/api/orders").param("status", "pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value("GU-0001"));

        orderMvc.perform(get("/api/orders/{id}", "GU-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deviceName").value("Máy lạnh Daikin"));

        orderMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                {"deviceName":"Máy lạnh Daikin","description":"Không lạnh","address":"123 Lê Lợi, Q1",
                 "estimatedPrice":300000,"serviceName":"Sửa máy lạnh"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("GU-0001"));

        orderMvc.perform(patch("/api/orders/{id}/status", "GU-0001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"in_progress\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("accepted"));

        orderMvc.perform(post("/api/orders/{id}/cancel", "GU-0001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Khách bận\"}"))
                .andExpect(status().isOk());

        orderMvc.perform(post("/api/orders/{id}/accept", "GU-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("accepted"));

        orderMvc.perform(post("/api/orders/{id}/reject", "GU-0001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Quá xa\"}"))
                .andExpect(status().isOk());

        orderMvc.perform(post("/api/orders/{id}/complete", "GU-0001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"finalPrice\":350000}"))
                .andExpect(status().isOk());

        orderMvc.perform(patch("/api/orders/{id}/price", "GU-0001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\":400000,\"reason\":\"Thay linh kiện\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("GU-0001"));

        orderMvc.perform(post("/api/orders/{id}/price/approve", "GU-0001"))
                .andExpect(status().isOk());

        orderMvc.perform(post("/api/orders/{id}/price/reject", "GU-0001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Giá cao\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ORDERS (negative) — create without required fields / negative price → 422")
    void orderValidationErrors() throws Exception {
        orderMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceName\":\"\",\"description\":\"\",\"address\":\"\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));

        orderMvc.perform(patch("/api/orders/{id}/price", "GU-0001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\":-100,\"reason\":\"\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ================================================================
    // TECHNICIANS  /api/technicians
    // ================================================================

    @Test
    @DisplayName("TECHNICIANS — list / detail / update profile / availability / reviews")
    void technicianApisReturnExpectedContracts() throws Exception {
        when(technicianService.listTechnicians(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(PagedResponse.of(List.of(TechnicianListItemResponse.builder()
                        .id("TECH-001").fullName("Trần Văn B").rating(BigDecimal.valueOf(4.8))
                        .isAvailable(true).build()), 1, 10, 1));
        when(technicianService.getTechnician("TECH-001")).thenReturn(TechnicianDetailResponse.builder()
                .id("TECH-001").fullName("Trần Văn B").isAvailable(true).build());
        when(technicianService.updateProfile(eq("TECH-001"), any())).thenReturn(TechnicianProfileUpdateResponse.builder()
                .id("TECH-001").fullName("Trần Văn B").bio("Thợ giỏi").build());
        when(technicianService.updateAvailability(eq("TECH-001"), any())).thenReturn(AvailabilityResponse.builder()
                .id("TECH-001").isAvailable(false).build());
        when(technicianService.listReviews(eq("TECH-001"), anyInt(), anyInt())).thenReturn(TechnicianReviewListResponse.builder()
                .averageRating(BigDecimal.valueOf(4.8)).totalReviews(10L).items(List.of()).build());

        technicianMvc.perform(get("/api/technicians").param("district", "Quận 1").param("minRating", "4.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value("TECH-001"));

        technicianMvc.perform(get("/api/technicians/{id}", "TECH-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Trần Văn B"));

        technicianMvc.perform(patch("/api/technicians/{id}/profile", "TECH-001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"Thợ giỏi\",\"pricePerHour\":150000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bio").value("Thợ giỏi"));

        technicianMvc.perform(patch("/api/technicians/{id}/availability", "TECH-001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isAvailable\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isAvailable").value(false));

        technicianMvc.perform(get("/api/technicians/{id}/reviews", "TECH-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalReviews").value(10));
    }

    @Test
    @DisplayName("TECHNICIANS (negative) — availability without isAvailable → 422")
    void technicianValidationErrors() throws Exception {
        technicianMvc.perform(patch("/api/technicians/{id}/availability", "TECH-001")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ================================================================
    // VERIFICATIONS  /api/verifications
    // ================================================================

    @Test
    @DisplayName("VERIFICATIONS — list / detail / review (JSON)")
    void verificationApisReturnExpectedContracts() throws Exception {
        when(verificationService.list(any(), any(), anyInt(), anyInt()))
                .thenReturn(PagedResponse.of(List.of(VerificationListItemResponse.builder()
                        .id("VR-001").technicianId("TECH-001").status("pending").build()), 1, 10, 1));
        when(verificationService.get("VR-001")).thenReturn(VerificationDetailResponse.builder()
                .id("VR-001").technicianId("TECH-001").status("pending").build());
        when(verificationService.review(eq("VR-001"), any())).thenReturn(VerificationReviewResponse.builder()
                .id("VR-001").status("approved").reviewedBy("Admin").build());

        verificationMvc.perform(get("/api/verifications").param("status", "pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value("VR-001"));

        verificationMvc.perform(get("/api/verifications/{id}", "VR-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("pending"));

        verificationMvc.perform(patch("/api/verifications/{id}", "VR-001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"approved\",\"note\":\"Hồ sơ hợp lệ\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("approved"));
    }

    // ================================================================
    // CONVERSATIONS + QUOTES  /api/conversations, /api/quotes
    // ================================================================

    @Test
    @DisplayName("CONVERSATIONS — list / create / messages / send / create quote")
    void conversationApisReturnExpectedContracts() throws Exception {
        when(conversationService.listConversations(anyInt(), anyInt()))
                .thenReturn(PagedResponse.of(List.of(ConversationListItemResponse.builder()
                        .id("CV-001").unreadCount(2L).build()), 1, 20, 1));
        when(conversationService.createConversation(any())).thenReturn(ConversationCreatedResponse.builder()
                .id("CV-001").participants(List.of("US-001", "TECH-001")).build());
        when(conversationService.listMessages(eq("CV-001"), anyInt(), anyInt()))
                .thenReturn(PagedResponse.of(List.of(MessageResponse.builder()
                        .id("MSG-001").conversationId("CV-001").content("Xin chào").build()), 1, 20, 1));
        when(conversationService.sendMessage(eq("CV-001"), any())).thenReturn(MessageResponse.builder()
                .id("MSG-002").conversationId("CV-001").content("Bạn cần hỗ trợ gì?").build());
        when(quotationService.createQuotation(eq("CV-001"), any())).thenReturn(QuotationResponse.builder()
                .id("QT-001").conversationId("CV-001").serviceName("Sửa máy lạnh").price(300000L).status("pending").build());

        conversationMvc.perform(get("/api/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value("CV-001"));

        conversationMvc.perform(post("/api/conversations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"technicianId\":\"TECH-001\",\"orderId\":\"GU-0001\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("CV-001"));

        conversationMvc.perform(get("/api/conversations/{id}/messages", "CV-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].content").value("Xin chào"));

        conversationMvc.perform(post("/api/conversations/{id}/messages", "CV-001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"text\",\"content\":\"Bạn cần hỗ trợ gì?\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("MSG-002"));

        conversationMvc.perform(post("/api/conversations/{id}/quotes", "CV-001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceName\":\"Sửa máy lạnh\",\"price\":300000}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("QT-001"));
    }

    @Test
    @DisplayName("CONVERSATIONS (negative) — empty message / quote without price → 422")
    void conversationValidationErrors() throws Exception {
        conversationMvc.perform(post("/api/conversations/{id}/messages", "CV-001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"\"}"))
                .andExpect(status().isUnprocessableEntity());

        conversationMvc.perform(post("/api/conversations/{id}/quotes", "CV-001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceName\":\"\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("QUOTES — accept quote")
    void quotationApisReturnExpectedContracts() throws Exception {
        when(quotationService.acceptQuotation("QT-001")).thenReturn(AcceptQuotationResponse.builder()
                .id("QT-001").status("accepted").orderId("GU-0002").build());

        quotationMvc.perform(patch("/api/quotes/{id}/accept", "QT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("accepted"))
                .andExpect(jsonPath("$.data.orderId").value("GU-0002"));
    }

    // ================================================================
    // REPORTS  /api/reports + /api/orders/{id}/reports
    // ================================================================

    @Test
    @DisplayName("REPORTS — admin list / create report on order")
    void reportApisReturnExpectedContracts() throws Exception {
        ReportResponse report = ReportResponse.builder().id("RP-001").orderId("GU-0001")
                .reason("Thợ trễ hẹn").status("open").build();
        when(reportService.listReports(any(), any(), anyInt(), anyInt()))
                .thenReturn(PagedResponse.of(List.of(report), 1, 10, 1));
        when(reportService.createReport(eq("GU-0001"), any())).thenReturn(report);

        reportMvc.perform(get("/api/reports").param("status", "open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value("RP-001"));

        orderReportMvc.perform(post("/api/orders/{id}/reports", "GU-0001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Thợ trễ hẹn\",\"description\":\"Trễ 2 tiếng\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("RP-001"));
    }

    @Test
    @DisplayName("REPORTS (negative) — create report without reason → 422")
    void reportValidationErrors() throws Exception {
        orderReportMvc.perform(post("/api/orders/{id}/reports", "GU-0001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"\",\"description\":\"\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ================================================================
    // REVIEWS  /api/orders/{id}/reviews
    // ================================================================

    @Test
    @DisplayName("REVIEWS — create review on order; rating out of range → 422")
    void reviewApisReturnExpectedContracts() throws Exception {
        when(reviewService.createReview(eq("GU-0001"), any())).thenReturn(ReviewResponse.builder()
                .id("RV-001").orderId("GU-0001").rating(5).content("Tuyệt vời").build());

        orderReviewMvc.perform(post("/api/orders/{id}/reviews", "GU-0001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"content\":\"Tuyệt vời\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.rating").value(5));

        // rating 6 > max 5
        orderReviewMvc.perform(post("/api/orders/{id}/reviews", "GU-0001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":6}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ================================================================
    // WARRANTY  /api/orders/{id}/warranty
    // ================================================================

    @Test
    @DisplayName("WARRANTY — create claim / get claim")
    void warrantyApisReturnExpectedContracts() throws Exception {
        WarrantyResponse warranty = WarrantyResponse.builder().id("WT-001").orderId("GU-0001")
                .status("active").description("Máy lại hỏng").remainingDays(60L).build();
        when(warrantyService.createWarranty(eq("GU-0001"), any())).thenReturn(warranty);
        when(warrantyService.getWarrantyByOrder("GU-0001")).thenReturn(warranty);

        orderWarrantyMvc.perform(post("/api/orders/{id}/warranty", "GU-0001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Máy lại hỏng\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("WT-001"));

        orderWarrantyMvc.perform(get("/api/orders/{id}/warranty", "GU-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remainingDays").value(60));
    }

    @Test
    @DisplayName("WARRANTY (negative) — claim without description → 422")
    void warrantyValidationErrors() throws Exception {
        orderWarrantyMvc.perform(post("/api/orders/{id}/warranty", "GU-0001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ================================================================
    // NOTIFICATIONS  /api/notifications
    // ================================================================

    @Test
    @DisplayName("NOTIFICATIONS — list / mark read / mark all read")
    void notificationApisReturnExpectedContracts() throws Exception {
        when(notificationService.getMyNotifications(anyInt(), anyInt())).thenReturn(NotificationListResponse.builder()
                .unreadCount(3L)
                .items(List.of(NotificationResponse.builder().id("NT-001").type("order").title("Đơn mới").isRead(false).build()))
                .pagination(PagedResponse.PaginationMeta.builder().page(1).limit(20).total(1).totalPages(1).build())
                .build());
        when(notificationService.markAsRead("NT-001")).thenReturn(MarkReadResponse.builder().id("NT-001").isRead(true).build());
        when(notificationService.markAllAsRead()).thenReturn(ReadAllNotificationResponse.builder().updatedCount(3).build());

        notificationMvc.perform(get("/api/notifications").param("page", "1").param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(3))
                .andExpect(jsonPath("$.data.items[0].id").value("NT-001"));

        // NOTE: field `boolean isRead` is serialized by Jackson as JSON key "read"
        // (the "is" prefix is stripped for primitive booleans).
        notificationMvc.perform(patch("/api/notifications/{id}/read", "NT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.read").value(true));

        notificationMvc.perform(patch("/api/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updatedCount").value(3));
    }

    // ================================================================
    // PAYMENT WEBHOOK  /api/payments/vnpay/ipn
    // ================================================================

    @Test
    @DisplayName("PAYMENT — VNPay IPN webhook returns RspCode/Message")
    void vnpayIpnReturnsAck() throws Exception {
        when(walletService.handleVnpayIpn(any())).thenReturn(VnpayIpnResponse.builder()
                .RspCode("00").Message("Confirm Success").build());

        // FINDING: VNPay expects the literal keys "RspCode"/"Message", but with no
        // @JsonProperty the fields serialize as "rspCode"/"message" (Jackson bean
        // naming). The webhook ACK body therefore uses lower-camel keys — see QA note.
        paymentMvc.perform(get("/api/payments/vnpay/ipn")
                        .param("vnp_TxnRef", "TOPUP-001")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_SecureHash", "abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rspCode").value("00"))
                .andExpect(jsonPath("$.message").value("Confirm Success"));
    }

    // ----------------------------------------------------------------
    // helpers
    // ----------------------------------------------------------------

    private UserResponse sampleUser() {
        return UserResponse.builder()
                .id(1L).code("US-001").fullName("Nguyễn Văn A").email("a@example.com")
                .phone("0901234567").role("customer").status("active").build();
    }

    private BankAccountResponse bankAccount() {
        return BankAccountResponse.builder()
                .id("BA-001").bankName("Vietcombank").accountNumber("0123456789")
                .accountOwner("NGUYEN VAN A").isDefault(true).createdAt(LocalDateTime.now()).build();
    }
}
