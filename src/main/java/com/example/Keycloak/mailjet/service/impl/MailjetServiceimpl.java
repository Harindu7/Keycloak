package com.example.Keycloak.mailjet.service.impl;


import com.example.Keycloak.mailjet.model.dto.EmailRequestDTO;
import com.example.Keycloak.mailjet.model.dto.EmailResponseDTO;
import com.example.Keycloak.mailjet.service.MailjetService;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MailjetServiceimpl implements MailjetService {
    
    private static final Logger logger = LoggerFactory.getLogger(MailjetServiceimpl.class);
    
    @Value("${mailjet.api.key}")
    private String apiKey;
    
    @Value("${mailjet.api.secret}")
    private String apiSecret;
    
    @Value("${mailjet.from.email}")
    private String fromEmail;
    
    @Value("${mailjet.from.name:API Test}")
    private String fromName;
    
    @Override
    public EmailResponseDTO sendEmail(EmailRequestDTO emailRequest) {
        try {
            logger.info("Attempting to send email to: {}", emailRequest.getTo());
            
            // Create Mailjet client
            MailjetClient client = new MailjetClient(
                ClientOptions.builder()
                    .apiKey(apiKey)
                    .apiSecretKey(apiSecret)
                    .build()
            );
            
            // Build email request
            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                    .put(new JSONObject()
                        .put(Emailv31.Message.FROM, new JSONObject()
                            .put("Email", fromEmail)
                            .put("Name", fromName))
                        .put(Emailv31.Message.TO, new JSONArray()
                            .put(new JSONObject()
                                .put("Email", emailRequest.getTo())))
                        .put(Emailv31.Message.SUBJECT, emailRequest.getSubject())
                        .put(Emailv31.Message.TEXTPART, emailRequest.getBody())
                        .put(Emailv31.Message.HTMLPART, 
                            "<div style='font-family: Arial, sans-serif; line-height: 1.6;'>" +
                            emailRequest.getBody().replace("\n", "<br>") +
                            "</div>")));
            
            // Send email
            MailjetResponse response = client.post(request);
            
            if (response.getStatus() == 200) {
                JSONArray messages = response.getData();
                if (messages.length() > 0) {
                    JSONObject message = messages.getJSONObject(0);
                    String messageId = message.optString("MessageID", "unknown");
                    
                    logger.info("Email sent successfully. Message ID: {}", messageId);
                    return EmailResponseDTO.success(messageId);
                } else {
                    logger.error("No message data in response");
                    return EmailResponseDTO.error("No message data in response");
                }
            } else {
                String errorMessage = "Failed to send email. Status: " + response.getStatus() + 
                                    ", Data: " + response.getData();
                logger.error(errorMessage);
                return EmailResponseDTO.error(errorMessage);
            }
            
        } catch (MailjetException e) {
            String errorMessage = "Mailjet API error: " + e.getMessage();
            logger.error(errorMessage, e);
            return EmailResponseDTO.error(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Unexpected error while sending email: " + e.getMessage();
            logger.error(errorMessage, e);
            return EmailResponseDTO.error(errorMessage);
        }
    }
    
    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() &&
               !apiKey.equals("your-mailjet-api-key") &&
               apiSecret != null && !apiSecret.trim().isEmpty() &&
               !apiSecret.equals("your-mailjet-api-secret") &&
               fromEmail != null && !fromEmail.trim().isEmpty() &&
               !fromEmail.equals("your-email@example.com");
    }

    @Override
    public void sendVerificationEmail(String toEmail, String firstName, String verificationLink) {
        try {
            logger.info("Sending verification email to: {}", toEmail);

            String subject = "Verify Your Email Address - Keycloak Demo";
            String htmlBody = buildVerificationEmailHtml(firstName, verificationLink);
            String textBody = buildVerificationEmailText(firstName, verificationLink);

            // Create Mailjet client
            MailjetClient client = new MailjetClient(
                ClientOptions.builder()
                    .apiKey(apiKey)
                    .apiSecretKey(apiSecret)
                    .build()
            );

            // Build email request
            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                    .put(new JSONObject()
                        .put(Emailv31.Message.FROM, new JSONObject()
                            .put("Email", fromEmail)
                            .put("Name", fromName))
                        .put(Emailv31.Message.TO, new JSONArray()
                            .put(new JSONObject()
                                .put("Email", toEmail)
                                .put("Name", firstName)))
                        .put(Emailv31.Message.SUBJECT, subject)
                        .put(Emailv31.Message.TEXTPART, textBody)
                        .put(Emailv31.Message.HTMLPART, htmlBody)));

            // Send email
            MailjetResponse response = client.post(request);

            if (response.getStatus() == 200) {
                logger.info("Verification email sent successfully to: {}", toEmail);
            } else {
                logger.error("Failed to send verification email. Status: {}, Data: {}",
                           response.getStatus(), response.getData());
                throw new RuntimeException("Failed to send verification email");
            }

        } catch (MailjetException e) {
            logger.error("Mailjet API error while sending verification email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email", e);
        } catch (Exception e) {
            logger.error("Unexpected error while sending verification email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private String buildVerificationEmailHtml(String firstName, String verificationLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    .email-container { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; }
                    .header { background-color: #0d6efd; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background-color: #f8f9fa; }
                    .button { background-color: #198754; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; }
                    .footer { padding: 20px; text-align: center; color: #6c757d; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="header">
                        <h1>🛡️ Keycloak Demo</h1>
                        <p>Email Verification Required</p>
                    </div>
                    <div class="content">
                        <h2>Hello %s!</h2>
                        <p>Thank you for registering with Keycloak Demo. To complete your registration, please verify your email address by clicking the button below:</p>
                        
                        <center>
                            <a href="%s" class="button">Verify Email Address</a>
                        </center>
                        
                        <p>If the button doesn't work, you can copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; color: #0d6efd;">%s</p>
                        
                        <p><strong>Important:</strong> This verification link will expire in 24 hours.</p>
                        
                        <p>If you didn't create an account with us, please ignore this email.</p>
                        
                        <p>Best regards,<br>The Keycloak Demo Team</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName, verificationLink, verificationLink);
    }

    private String buildVerificationEmailText(String firstName, String verificationLink) {
        return String.format("""
            Hello %s!
            
            Thank you for registering with Keycloak Demo. To complete your registration, please verify your email address by clicking the link below:
            
            %s
            
            Important: This verification link will expire in 24 hours.
            
            If you didn't create an account with us, please ignore this email.
            
            Best regards,
            The Keycloak Demo Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """, firstName, verificationLink);
    }
}
