package com.campusmate.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails via Brevo API (no SMTP)
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    @Value("${app.mail.from:kumarhirtik3@gmail.com}")
    private String fromEmail;

    @Value("${app.frontend.url:https://campusmatefrontend.netlify.app}")
    private String frontendUrl;

    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";

    /**
     * Send verification email
     */
    public void sendVerificationEmail(String toEmail, String token, String firstName) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;

        // Escape properly: no newlines inside quoted JSON strings
        String htmlContent = String.format(
                "<p>Hello %s,</p>" +
                "<p>Thank you for registering with CampusMate!</p>" +
                "<p>Please verify your email by clicking <a href='%s'>here</a>.</p>" +
                "<p>This link will expire in 24 hours.</p>",
                firstName, verificationUrl);

        String jsonPayload = String.format("""
            {
              "sender": { "name": "CampusMate", "email": "%s" },
              "to": [ { "email": "%s" } ],
              "subject": "Verify Your Email - CampusMate",
              "htmlContent": "%s"
            }
            """, fromEmail, toEmail, escapeJson(htmlContent));

        sendRequest(jsonPayload, toEmail, "verification");
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String token, String firstName) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        String htmlContent = String.format(
                "<p>Hello %s,</p>" +
                "<p>You requested a password reset for your CampusMate account.</p>" +
                "<p>Click <a href='%s'>here</a> to reset your password.</p>" +
                "<p>This link will expire in 1 hour.</p>",
                firstName, resetUrl);

        String jsonPayload = String.format("""
            {
              "sender": { "name": "CampusMate", "email": "%s" },
              "to": [ { "email": "%s" } ],
              "subject": "Reset Your Password - CampusMate",
              "htmlContent": "%s"
            }
            """, fromEmail, toEmail, escapeJson(htmlContent));

        sendRequest(jsonPayload, toEmail, "password reset");
    }

    /**
     * Generic HTTP request to Brevo
     */
    private void sendRequest(String jsonPayload, String toEmail, String type) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("accept", "application/json")
                    .header("api-key", brevoApiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("✅ {} email sent successfully to {}", type, toEmail);
            } else {
                log.error("❌ Failed to send {} email to {}. Response: {}", type, toEmail, response.body());
                throw new RuntimeException("Brevo email sending failed: " + response.body());
            }

        } catch (Exception e) {
            log.error("❌ Exception while sending {} email to {}", type, toEmail, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    /**
     * Escape double quotes and newlines for safe JSON string embedding
     */
    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "")
                .replace("\r", "");
    }
}
