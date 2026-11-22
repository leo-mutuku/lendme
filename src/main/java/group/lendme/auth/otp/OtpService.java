package group.lendme.auth.otp;

import group.lendme.config.SecurityProperties;
import group.lendme.notification.EmailService;
import group.lendme.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

/**
 * Issues and checks 6-digit email OTPs.
 */
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpCodeRepository otpCodes;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecurityProperties props;

    public enum IssueResult { SENT, COOLDOWN }

    /**
     * Creates a new code (replacing any open one) and emails it.
     * Returns COOLDOWN without sending if a code was sent too recently.
     */
    @Transactional
    public IssueResult issue(User user, OtpPurpose purpose) {
        Instant now = Instant.now();
        SecurityProperties.Otp cfg = props.otp();

        boolean tooSoon = otpCodes.findFirstByUserIdAndPurposeOrderByCreatedAtDesc(user.getId(), purpose)
                .map(last -> last.getCreatedAt().plus(cfg.resendCooldown()).isAfter(now))
                .orElse(false);
        if (tooSoon) {
            return IssueResult.COOLDOWN;
        }

        otpCodes.consumeAllOpen(user.getId(), purpose, now);

        String code = "%06d".formatted(RANDOM.nextInt(1_000_000));
        otpCodes.save(new OtpCode(user.getId(), purpose, passwordEncoder.encode(code), now.plus(cfg.ttl())));

        emailService.sendOtp(user.getEmail().value(), user.getFirstName(), purpose, code, cfg.ttl());
        return IssueResult.SENT;
    }

    /**
     * Checks the code and consumes it on success.
     * Runs in its own transaction so a wrong guess is counted even if the caller then fails.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean verify(User user, OtpPurpose purpose, String code) {
        Instant now = Instant.now();
        int maxAttempts = props.otp().maxAttempts();

        return otpCodes.findFirstByUserIdAndPurposeOrderByCreatedAtDesc(user.getId(), purpose)
                .filter(otp -> otp.isUsable(now, maxAttempts))
                .map(otp -> {
                    if (passwordEncoder.matches(code, otp.getCodeHash())) {
                        otp.consume(now);
                        return true;
                    }
                    otp.recordFailedAttempt();
                    return false;
                })
                .orElse(false);
    }

    public Duration resendCooldown() {
        return props.otp().resendCooldown();
    }
}
