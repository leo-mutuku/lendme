package group.lendme.notification;

import group.lendme.auth.otp.OtpPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Sends transactional emails.
 * If no SMTP host is configured (MAIL_HOST unset), the email is written to the log
 * instead, so registration and password reset can be tested locally.
 */
@Slf4j
@Service
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String mailHost;
    private final String from;

    public EmailService(ObjectProvider<JavaMailSender> mailSender,
                        @Value("${spring.mail.host:}") String mailHost,
                        @Value("${lendme.mail.from}") String from) {
        this.mailSender = mailSender;
        this.mailHost = mailHost;
        this.from = from;
    }

    public void sendOtp(String to, String firstName, OtpPurpose purpose, String code, Duration ttl) {
        String subject = switch (purpose) {
            case EMAIL_VERIFICATION -> "Verify your LendMe email";
            case PASSWORD_RESET -> "Reset your LendMe password";
        };
        String action = switch (purpose) {
            case EMAIL_VERIFICATION -> "verify your email address";
            case PASSWORD_RESET -> "reset your password";
        };
        String body = """
                Hi %s,

                Your LendMe code to %s is: %s

                It expires in %d minutes. If you did not request this, you can ignore this email.
                """.formatted(firstName, action, code, ttl.toMinutes());

        send(to, subject, body);
    }

    private void send(String to, String subject, String body) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (mailHost.isBlank() || sender == null) {
            log.warn("MAIL_HOST not set - email NOT sent. To: {} | Subject: {}\n{}", to, subject, body);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        sender.send(message);
    }
}
