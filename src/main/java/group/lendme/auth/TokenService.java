package group.lendme.auth;

import group.lendme.config.JwtConfig;
import group.lendme.config.SecurityProperties;
import group.lendme.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

/** Creates signed access tokens. Verification is done by Spring's JwtDecoder (see JwtConfig). */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final SecurityProperties props;

    public AccessToken issueAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(props.jwt().accessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.jwt().issuer())
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("email", user.getEmail() == null ? "" : user.getEmail().value())
                .claim("roles", user.getRoles().stream().map(Enum::name).toList())
                .build();

        JwsHeader header = JwsHeader.with(JwtConfig.ALGORITHM).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AccessToken(token, expiresAt);
    }

    public record AccessToken(String value, Instant expiresAt) {}
}
