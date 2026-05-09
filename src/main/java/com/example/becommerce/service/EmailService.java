package com.example.becommerce.service;

/**
 * Email service contract — defines email operations.
 */
public interface EmailService {

    /** Send registration confirmation email with verification link */
    void sendConfirmationEmail(String email, String fullName, String confirmationToken);

    /** Send password reset email */
    void sendPasswordResetEmail(String email, String resetToken);

    /** Send notification email */
    void sendNotificationEmail(String email, String subject, String body);
}

