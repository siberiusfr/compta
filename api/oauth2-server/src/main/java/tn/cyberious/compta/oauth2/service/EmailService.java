package tn.cyberious.compta.oauth2.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails.
 *
 * <p>Provides email functionality for password reset, email verification, etc.
 */
@Service
public class EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;

  @Value("${spring.mail.from:noreply@compta.tn}")
  private String fromEmail;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /**
   * Send password reset email.
   *
   * @param to The recipient email address
   * @param username The username
   * @param resetLink The password reset link
   */
  public void sendPasswordResetEmail(String to, String username, String resetLink) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(to);
      message.setSubject("Password Reset Request - Compta");

      String text = buildPasswordResetEmailText(username, resetLink);
      message.setText(text);

      mailSender.send(message);

      log.info("Password reset email sent to: {}", to);

    } catch (Exception e) {
      log.error("Failed to send password reset email to: {}", to, e);
      throw new RuntimeException("Failed to send password reset email", e);
    }
  }

  /**
   * Send email verification email.
   *
   * @param to The recipient email address
   * @param username The username
   * @param verificationLink The email verification link
   */
  public void sendEmailVerificationEmail(String to, String username, String verificationLink) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(to);
      message.setSubject("Email Verification - Compta");

      String text = buildEmailVerificationEmailText(username, verificationLink);
      message.setText(text);

      mailSender.send(message);

      log.info("Email verification email sent to: {}", to);

    } catch (Exception e) {
      log.error("Failed to send email verification email to: {}", to, e);
      throw new RuntimeException("Failed to send email verification email", e);
    }
  }

  /**
   * Send HTML email.
   *
   * @param to The recipient email address
   * @param subject The email subject
   * @param htmlContent The HTML content
   */
  public void sendHtmlEmail(String to, String subject, String htmlContent) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlContent, true); // true = HTML

      mailSender.send(message);

      log.info("HTML email sent to: {}", to);

    } catch (Exception e) {
      log.error("Failed to send HTML email to: {}", to, e);
      throw new RuntimeException("Failed to send HTML email", e);
    }
  }

  /** Build password reset email text. */
  private String buildPasswordResetEmailText(String username, String resetLink) {
    return String.format(
        """
        Hello %s,

        You have requested to reset your password for Compta.

        Please click the link below to reset your password:

        %s

        This link will expire in 1 hour.

        If you did not request this password reset, please ignore this email.

        Best regards,
        The Compta Team
        """,
        username, resetLink);
  }

  /** Build email verification email text. */
  private String buildEmailVerificationEmailText(String username, String verificationLink) {
    return String.format(
        """
        Hello %s,

        Thank you for registering with Compta!

        Please click the link below to verify your email address:

        %s

        This link will expire in 24 hours.

        If you did not create an account, please ignore this email.

        Best regards,
        The Compta Team
        """,
        username, verificationLink);
  }
}
