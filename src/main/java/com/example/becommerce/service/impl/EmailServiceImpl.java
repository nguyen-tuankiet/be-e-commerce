package com.example.becommerce.service.impl;

import com.example.becommerce.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Implementation of EmailService using Spring Mail.
 * Design system matches GlowUp frontend:
 *   Primary   #1a1e3b  (blueberry dark)
 *   Accent    #DEC38D  (gold)
 *   Secondary #2a315e  (blueberry light)
 *   Text sub  #94a3b8
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.name:GlowUp}")
    private String appName;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // ─────────────────────────────────────────────
    //  Public API
    // ─────────────────────────────────────────────

    @Override
    public void sendConfirmationEmail(String email, String fullName, String confirmationToken) {
        try {
            String url = frontendUrl + "/verify-email?token=" + confirmationToken;
            sendHtmlEmail(email,
                    "Xác nhận tài khoản " + appName,
                    buildConfirmationEmailHtml(fullName, url));
            log.info("Confirmation email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send confirmation email to {}: {}", email, e.getMessage(), e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String email, String resetToken) {
        try {
            String url = frontendUrl + "/reset-password?token=" + resetToken;
            sendHtmlEmail(email,
                    "Đặt lại mật khẩu - " + appName,
                    buildPasswordResetEmailHtml(url));
            log.info("Password reset email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage(), e);
        }
    }

    @Override
    public void sendNotificationEmail(String email, String subject, String body) {
        try {
            sendHtmlEmail(email, subject, buildNotificationEmailHtml(subject, body));
            log.info("Notification email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send notification email to {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Gửi email thông báo đơn hàng mới được nhận.
     * Gọi khi thợ accept đơn.
     */
    public void sendOrderAcceptedEmail(String email, String customerName,
                                       String orderId, String technicianName,
                                       String scheduledTime) {
        try {
            sendHtmlEmail(email,
                    "Đơn hàng #" + orderId + " đã được nhận - " + appName,
                    buildOrderAcceptedEmailHtml(customerName, orderId, technicianName, scheduledTime));
            log.info("Order accepted email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send order accepted email to {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Gửi email thông báo đơn hàng hoàn thành.
     */
    public void sendOrderCompletedEmail(String email, String customerName,
                                        String orderId, long finalPrice) {
        try {
            sendHtmlEmail(email,
                    "Đơn hàng #" + orderId + " hoàn thành - " + appName,
                    buildOrderCompletedEmailHtml(customerName, orderId, finalPrice));
            log.info("Order completed email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send order completed email to {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Gửi email xác nhận nạp tiền thành công.
     */
    public void sendTopUpSuccessEmail(String email, String fullName,
                                      long amount, String transactionId) {
        try {
            sendHtmlEmail(email,
                    "Nạp tiền thành công - " + appName,
                    buildTopUpSuccessEmailHtml(fullName, amount, transactionId));
            log.info("Top-up success email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send top-up email to {}: {}", email, e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────
    //  Core sender
    // ─────────────────────────────────────────────

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(senderEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    // ─────────────────────────────────────────────
    //  Shared layout helpers
    // ─────────────────────────────────────────────

    /** Wrapper toàn bộ email: header logo + body + footer */
    private String wrapLayout(String bodyContent) {
        return "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "  <meta charset='UTF-8'/>" +
                "  <meta name='viewport' content='width=device-width,initial-scale=1'/>" +
                "  <title>" + appName + "</title>" +
                "</head>" +
                "<body style='" +
                "  margin:0;padding:0;" +
                "  background-color:#f0f2f5;" +
                "  font-family:'Segoe UI', Tahoma, Arial, sans-serif;;" +
                "  -webkit-font-smoothing:antialiased;" +
                "'>" +

                // ── Outer wrapper
                "<table width='100%' cellpadding='0' cellspacing='0' border='0'" +
                "  style='background-color:#f0f2f5;padding:40px 16px'>" +
                "<tr><td align='center'>" +

                // ── Card
                "<table width='600' cellpadding='0' cellspacing='0' border='0'" +
                "  style='max-width:600px;width:100%;border-radius:20px;overflow:hidden;" +
                "         box-shadow:0 20px 40px rgba(26,30,59,0.12)'>" +

                // ── Header
                "<tr><td style='" +
                "  background:linear-gradient(135deg,#1a1e3b 0%,#2a315e 100%);" +
                "  padding:36px 40px 28px;text-align:center'>" +

                // Logo mark
                "<div style='" +
                "  display:inline-block;" +
                "  background:rgba(222,195,141,0.18);" +
                "  border:1px solid rgba(222,195,141,0.35);" +
                "  border-radius:14px;" +
                "  padding:10px 22px;" +
                "  margin-bottom:14px'>" +
                "<span style='font-size:26px;font-weight:800;color:#DEC38D;letter-spacing:-0.5px'>" +
                appName +
                "</span>" +
                "</div>" +

                "<br/>" +
                "<span style='font-size:12px;color:rgba(255,255,255,0.55);letter-spacing:1.5px;text-transform:uppercase'>" +
                "Nền tảng dịch vụ nhà cửa cao cấp" +
                "</span>" +
                "</td></tr>" +

                // ── Body
                "<tr><td style='background:#ffffff;padding:40px'>" +
                bodyContent +
                "</td></tr>" +

                // ── Footer
                "<tr><td style='" +
                "  background:#f8fafc;" +
                "  border-top:1px solid #e2e8f0;" +
                "  padding:24px 40px;" +
                "  text-align:center'>" +

                "<p style='margin:0 0 6px;font-size:13px;color:#64748b'>" +
                "&copy; 2026 " + appName + ". All rights reserved." +
                "</p>" +
                "<p style='margin:0;font-size:12px;color:#94a3b8'>" +
                "Email này được gửi tự động, vui lòng không trả lời." +
                "</p>" +
                "</td></tr>" +

                "</table>" + // end card
                "</td></tr>" +
                "</table>" + // end outer
                "</body></html>";
    }

    /** Nút CTA chính — gold */
    private String ctaButton(String url, String label) {
        return "<table cellpadding='0' cellspacing='0' border='0' style='margin:28px auto'>" +
                "<tr><td align='center' style='" +
                "  background:#DEC38D;" +
                "  border-radius:12px;" +
                "  box-shadow:0 8px 20px rgba(222,195,141,0.35)'>" +
                "<a href='" + url + "' style='" +
                "  display:inline-block;" +
                "  padding:14px 36px;" +
                "  font-size:15px;" +
                "  font-weight:700;" +
                "  color:#000C33;" +
                "  text-decoration:none;" +
                "  letter-spacing:0.2px'>" +
                label +
                "</a>" +
                "</td></tr>" +
                "</table>";
    }

    /** Badge trạng thái nhỏ */
    private String badge(String text, String bgColor, String textColor) {
        return "<span style='" +
                "  display:inline-block;" +
                "  background:" + bgColor + ";" +
                "  color:" + textColor + ";" +
                "  font-size:12px;" +
                "  font-weight:700;" +
                "  padding:4px 12px;" +
                "  border-radius:999px;" +
                "  letter-spacing:0.5px;" +
                "  text-transform:uppercase'>" +
                text +
                "</span>";
    }

    /** Divider */
    private String divider() {
        return "<hr style='border:none;border-top:1px solid #f1f5f9;margin:28px 0'/>";
    }

    /** Info row dùng trong order detail */
    private String infoRow(String label, String value) {
        return "<tr>" +
                "<td style='padding:10px 0;font-size:13px;color:#94a3b8;width:40%'>" + label + "</td>" +
                "<td style='padding:10px 0;font-size:14px;color:#1a1e3b;font-weight:600'>" + value + "</td>" +
                "</tr>";
    }

    /** Fallback link */
    private String fallbackLink(String url) {
        return "<p style='margin:16px 0 0;font-size:12px;color:#94a3b8;text-align:center'>" +
                "Nếu nút không hoạt động, sao chép link sau vào trình duyệt:<br/>" +
                "<a href='" + url + "' style='color:#3b82f6;word-break:break-all'>" + url + "</a>" +
                "</p>";
    }

    // ─────────────────────────────────────────────
    //  Email templates
    // ─────────────────────────────────────────────

    /**
     * Template 1: Xác nhận email đăng ký
     */
    private String buildConfirmationEmailHtml(String fullName, String confirmationUrl) {
        String body =
                // Greeting
                "<h2 style='margin:0 0 8px;font-size:24px;font-weight:800;color:#1a1e3b;letter-spacing:-0.5px'>" +
                        "Xác nhận tài khoản của bạn 👋" +
                        "</h2>" +
                        "<p style='margin:0 0 24px;font-size:15px;color:#64748b;line-height:1.6'>" +
                        "Xin chào <strong style='color:#1a1e3b'>" + fullName + "</strong>,<br/>" +
                        "Cảm ơn bạn đã đăng ký tài khoản tại <strong>" + appName + "</strong>. " +
                        "Vui lòng xác nhận địa chỉ email để kích hoạt tài khoản và bắt đầu trải nghiệm dịch vụ." +
                        "</p>" +

                        // Feature highlights
                        "<table width='100%' cellpadding='0' cellspacing='0' border='0'" +
                        "  style='background:#f8fafc;border-radius:14px;padding:20px;margin-bottom:24px'>" +
                        "<tr>" +
                        featureItem("✅", "Đặt lịch sửa chữa nhanh chóng") +
                        "</tr><tr>" +
                        featureItem("🔧", "Kỹ thuật viên được xác minh 100%") +
                        "</tr><tr>" +
                        featureItem("⭐", "Đánh giá minh bạch, đảm bảo chất lượng") +
                        "</tr>" +
                        "</table>" +

                        // CTA
                        "<p style='margin:0;font-size:14px;color:#64748b;text-align:center'>" +
                        "Nhấn vào nút bên dưới để xác nhận email của bạn:" +
                        "</p>" +
                        ctaButton(confirmationUrl, "Xác nhận Email →") +
                        fallbackLink(confirmationUrl) +

                        divider() +

                        "<p style='margin:0;font-size:12px;color:#94a3b8;text-align:center'>" +
                        "⏰ Liên kết này sẽ hết hạn sau <strong>24 giờ</strong>. " +
                        "Nếu bạn không đăng ký tài khoản, vui lòng bỏ qua email này." +
                        "</p>";

        return wrapLayout(body);
    }

    private String featureItem(String icon, String text) {
        return "<td style='padding:6px 0'>" +
                "<span style='font-size:16px;margin-right:10px'>" + icon + "</span>" +
                "<span style='font-size:14px;color:#475569'>" + text + "</span>" +
                "</td>";
    }

    /**
     * Template 2: Đặt lại mật khẩu
     */
    private String buildPasswordResetEmailHtml(String resetUrl) {
        String body =
                // Warning icon block
                "<div style='" +
                        "  background:linear-gradient(135deg,#fff7ed,#fef3c7);" +
                        "  border:1px solid #fde68a;" +
                        "  border-radius:14px;" +
                        "  padding:18px 20px;" +
                        "  margin-bottom:28px;" +
                        "  display:flex;align-items:center'>" +
                        "<span style='font-size:28px;margin-right:14px'>🔐</span>" +
                        "<div>" +
                        "<strong style='font-size:14px;color:#92400e;display:block;margin-bottom:4px'>Yêu cầu đặt lại mật khẩu</strong>" +
                        "<span style='font-size:13px;color:#b45309'>Chúng tôi nhận được yêu cầu từ tài khoản của bạn</span>" +
                        "</div>" +
                        "</div>" +

                        "<h2 style='margin:0 0 12px;font-size:24px;font-weight:800;color:#1a1e3b;letter-spacing:-0.5px'>" +
                        "Đặt lại mật khẩu" +
                        "</h2>" +
                        "<p style='margin:0 0 24px;font-size:15px;color:#64748b;line-height:1.6'>" +
                        "Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản <strong>" + appName + "</strong>. " +
                        "Nhấn vào nút bên dưới để tạo mật khẩu mới:" +
                        "</p>" +

                        ctaButton(resetUrl, "Đặt lại mật khẩu →") +
                        fallbackLink(resetUrl) +

                        divider() +

                        // Security tips
                        "<p style='margin:0 0 12px;font-size:13px;font-weight:700;color:#1a1e3b'>💡 Lưu ý bảo mật:</p>" +
                        "<ul style='margin:0;padding-left:20px;font-size:13px;color:#64748b;line-height:1.8'>" +
                        "<li>Liên kết này chỉ có hiệu lực trong <strong>1 giờ</strong></li>" +
                        "<li>Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này</li>" +
                        "<li>Tài khoản của bạn vẫn an toàn cho đến khi mật khẩu được thay đổi</li>" +
                        "</ul>";

        return wrapLayout(body);
    }

    /**
     * Template 3: Thông báo chung
     */
    private String buildNotificationEmailHtml(String subject, String bodyText) {
        String body =
                "<h2 style='margin:0 0 16px;font-size:22px;font-weight:800;color:#1a1e3b;letter-spacing:-0.5px'>" +
                        subject +
                        "</h2>" +
                        "<div style='font-size:15px;color:#475569;line-height:1.7'>" +
                        bodyText +
                        "</div>" +
                        divider() +
                        "<p style='margin:0;font-size:13px;color:#94a3b8;text-align:center'>" +
                        "Truy cập " +
                        "<a href='" + frontendUrl + "' style='color:#3b82f6;font-weight:600'>" + appName + "</a>" +
                        " để xem thêm chi tiết." +
                        "</p>";

        return wrapLayout(body);
    }

    /**
     * Template 4: Đơn hàng đã được thợ nhận
     */
    private String buildOrderAcceptedEmailHtml(String customerName, String orderId,
                                               String technicianName, String scheduledTime) {
        String orderUrl = frontendUrl + "/customer/order-management";

        String body =
                // Status badge
                "<div style='text-align:center;margin-bottom:24px'>" +
                        badge("Đơn hàng đã được nhận", "#dcfce7", "#166534") +
                        "</div>" +

                        "<h2 style='margin:0 0 8px;font-size:24px;font-weight:800;color:#1a1e3b;text-align:center;letter-spacing:-0.5px'>" +
                        "Kỹ thuật viên đang trên đường đến! 🔧" +
                        "</h2>" +
                        "<p style='margin:0 0 28px;font-size:15px;color:#64748b;line-height:1.6;text-align:center'>" +
                        "Xin chào <strong style='color:#1a1e3b'>" + customerName + "</strong>,<br/>" +
                        "Đơn hàng của bạn đã được kỹ thuật viên chấp nhận. Mọi thứ đang được sắp xếp!" +
                        "</p>" +

                        // Order info card
                        "<div style='" +
                        "  background:linear-gradient(135deg,#1a1e3b 0%,#2a315e 100%);" +
                        "  border-radius:16px;" +
                        "  padding:24px 28px;" +
                        "  margin-bottom:24px'>" +

                        "<p style='margin:0 0 16px;font-size:12px;color:rgba(255,255,255,0.55);letter-spacing:1px;text-transform:uppercase'>" +
                        "Chi tiết đơn hàng" +
                        "</p>" +

                        "<table width='100%' cellpadding='0' cellspacing='0' border='0'>" +
                        infoRowDark("Mã đơn hàng", "#" + orderId) +
                        infoRowDark("Kỹ thuật viên", technicianName) +
                        infoRowDark("Thời gian hẹn", scheduledTime) +
                        "</table>" +
                        "</div>" +

                        // Tips
                        "<div style='background:#f0fdf4;border-radius:12px;padding:16px 20px;margin-bottom:24px'>" +
                        "<p style='margin:0 0 10px;font-size:13px;font-weight:700;color:#166534'>📋 Chuẩn bị trước khi thợ đến:</p>" +
                        "<ul style='margin:0;padding-left:18px;font-size:13px;color:#166534;line-height:1.8'>" +
                        "<li>Chuẩn bị không gian cho thợ làm việc</li>" +
                        "<li>Có mặt tại địa điểm trước giờ hẹn 5 phút</li>" +
                        "<li>Sẵn sàng mô tả chi tiết tình trạng thiết bị</li>" +
                        "</ul>" +
                        "</div>" +

                        ctaButton(orderUrl, "Theo dõi đơn hàng →") +

                        divider() +

                        "<p style='margin:0;font-size:12px;color:#94a3b8;text-align:center'>" +
                        "Cần hỗ trợ? Liên hệ hotline <strong style='color:#1a1e3b'>1900 8888</strong> hoặc chat trực tiếp trong ứng dụng." +
                        "</p>";

        return wrapLayout(body);
    }

    private String infoRowDark(String label, String value) {
        return "<tr>" +
                "<td style='padding:8px 0;font-size:13px;color:rgba(255,255,255,0.55);width:45%'>" + label + "</td>" +
                "<td style='padding:8px 0;font-size:14px;color:#ffffff;font-weight:700'>" + value + "</td>" +
                "</tr>";
    }

    /**
     * Template 5: Đơn hàng hoàn thành
     */
    private String buildOrderCompletedEmailHtml(String customerName, String orderId, long finalPrice) {
        String reviewUrl = frontendUrl + "/customer/order-management";
        String formattedPrice = String.format("%,d", finalPrice).replace(",", ".") + "đ";

        String body =
                "<div style='text-align:center;margin-bottom:24px'>" +
                        badge("Hoàn thành", "#dbeafe", "#1d4ed8") +
                        "</div>" +

                        "<h2 style='margin:0 0 8px;font-size:24px;font-weight:800;color:#1a1e3b;text-align:center;letter-spacing:-0.5px'>" +
                        "Dịch vụ đã hoàn thành! ✅" +
                        "</h2>" +
                        "<p style='margin:0 0 28px;font-size:15px;color:#64748b;line-height:1.6;text-align:center'>" +
                        "Xin chào <strong style='color:#1a1e3b'>" + customerName + "</strong>,<br/>" +
                        "Đơn hàng <strong>#" + orderId + "</strong> đã được hoàn thành. Cảm ơn bạn đã tin tưởng " + appName + "!" +
                        "</p>" +

                        // Payment summary
                        "<div style='" +
                        "  background:#f8fafc;" +
                        "  border:1px solid #e2e8f0;" +
                        "  border-radius:16px;" +
                        "  padding:24px 28px;" +
                        "  margin-bottom:24px'>" +

                        "<p style='margin:0 0 16px;font-size:12px;color:#94a3b8;letter-spacing:1px;text-transform:uppercase'>Hoá đơn dịch vụ</p>" +

                        "<table width='100%' cellpadding='0' cellspacing='0' border='0'>" +
                        infoRow("Mã đơn hàng", "#" + orderId) +
                        infoRow("Trạng thái thanh toán", "✅ Đã thanh toán") +
                        "</table>" +

                        "<hr style='border:none;border-top:1px solid #e2e8f0;margin:16px 0'/>" +

                        "<table width='100%' cellpadding='0' cellspacing='0' border='0'>" +
                        "<tr>" +
                        "<td style='font-size:15px;font-weight:700;color:#1a1e3b'>Tổng thanh toán</td>" +
                        "<td style='font-size:22px;font-weight:800;color:#1a1e3b;text-align:right'>" + formattedPrice + "</td>" +
                        "</tr>" +
                        "</table>" +
                        "</div>" +

                        // Rating request
                        "<div style='" +
                        "  background:linear-gradient(135deg,#1a1e3b 0%,#2a315e 100%);" +
                        "  border-radius:16px;" +
                        "  padding:24px 28px;" +
                        "  text-align:center;" +
                        "  margin-bottom:24px'>" +

                        "<p style='margin:0 0 6px;font-size:22px'>⭐⭐⭐⭐⭐</p>" +
                        "<p style='margin:0 0 4px;font-size:16px;font-weight:700;color:#ffffff'>Bạn có hài lòng không?</p>" +
                        "<p style='margin:0 0 18px;font-size:13px;color:rgba(255,255,255,0.65)'>" +
                        "Đánh giá của bạn giúp cộng đồng GlowUp ngày càng tốt hơn" +
                        "</p>" +
                        "<a href='" + reviewUrl + "' style='" +
                        "  display:inline-block;" +
                        "  background:#DEC38D;" +
                        "  color:#000C33;" +
                        "  font-size:14px;" +
                        "  font-weight:700;" +
                        "  padding:12px 28px;" +
                        "  border-radius:10px;" +
                        "  text-decoration:none'>" +
                        "Gửi đánh giá ngay →" +
                        "</a>" +
                        "</div>" +

                        "<p style='margin:0;font-size:12px;color:#94a3b8;text-align:center'>" +
                        "Nếu bạn gặp bất kỳ vấn đề gì, hãy yêu cầu bảo hành trong vòng 3 tháng." +
                        "</p>";

        return wrapLayout(body);
    }

    /**
     * Template 6: Nạp tiền thành công
     */
    private String buildTopUpSuccessEmailHtml(String fullName, long amount, String transactionId) {
        String formattedAmount = String.format("%,d", amount).replace(",", ".") + "đ";
        String walletUrl = frontendUrl + "/technician/wallet";

        String body =
                "<div style='text-align:center;margin-bottom:24px'>" +
                        badge("Giao dịch thành công", "#dcfce7", "#166534") +
                        "</div>" +

                        "<h2 style='margin:0 0 8px;font-size:24px;font-weight:800;color:#1a1e3b;text-align:center;letter-spacing:-0.5px'>" +
                        "Nạp tiền thành công! 💰" +
                        "</h2>" +
                        "<p style='margin:0 0 28px;font-size:15px;color:#64748b;line-height:1.6;text-align:center'>" +
                        "Xin chào <strong style='color:#1a1e3b'>" + fullName + "</strong>,<br/>" +
                        "Ví GlowUp của bạn đã được nạp tiền thành công." +
                        "</p>" +

                        // Amount highlight
                        "<div style='" +
                        "  background:linear-gradient(135deg,#1a1e3b 0%,#2a315e 100%);" +
                        "  border-radius:20px;" +
                        "  padding:32px;" +
                        "  text-align:center;" +
                        "  margin-bottom:24px'>" +

                        "<p style='margin:0 0 6px;font-size:12px;color:rgba(255,255,255,0.55);letter-spacing:1.5px;text-transform:uppercase'>Số tiền đã nạp</p>" +
                        "<p style='margin:0 0 16px;font-size:42px;font-weight:800;color:#DEC38D;letter-spacing:-1px'>" + formattedAmount + "</p>" +

                        "<div style='background:rgba(255,255,255,0.08);border-radius:10px;padding:12px 16px;display:inline-block'>" +
                        "<span style='font-size:12px;color:rgba(255,255,255,0.6)'>Mã giao dịch: </span>" +
                        "<span style='font-size:13px;color:#ffffff;font-weight:600'>" + transactionId + "</span>" +
                        "</div>" +
                        "</div>" +

                        // Transaction detail
                        "<div style='" +
                        "  background:#f8fafc;" +
                        "  border:1px solid #e2e8f0;" +
                        "  border-radius:14px;" +
                        "  padding:20px 24px;" +
                        "  margin-bottom:24px'>" +

                        "<table width='100%' cellpadding='0' cellspacing='0' border='0'>" +
                        infoRow("Loại giao dịch", "Nạp tiền vào ví") +
                        infoRow("Trạng thái", "✅ Thành công") +
                        infoRow("Thời gian xử lý", "Tức thì") +
                        "</table>" +
                        "</div>" +

                        ctaButton(walletUrl, "Xem ví của tôi →") +

                        divider() +

                        "<p style='margin:0;font-size:12px;color:#94a3b8;text-align:center'>" +
                        "Nếu bạn không thực hiện giao dịch này, hãy liên hệ ngay hotline <strong style='color:#1a1e3b'>1900 8888</strong>." +
                        "</p>";

        return wrapLayout(body);
    }
}