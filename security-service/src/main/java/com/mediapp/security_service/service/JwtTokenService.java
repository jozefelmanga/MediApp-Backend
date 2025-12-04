package com.mediapp.security_service.service;

import com.mediapp.security_service.config.RsaKeyManager;
import com.mediapp.security_service.config.properties.JwtProperties;
import com.mediapp.security_service.domain.AppUser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Creates signed JWT access tokens for authenticated principals.
 */
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private final RsaKeyManager rsaKeyManager;

    public AccessToken issueAccessToken(AppUser user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.getAccessTokenTtl());

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .issuer(jwtProperties.getIssuer())
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles().stream().map(Enum::name).toList())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(rsaKeyManager.keyId())
                        .type(JOSEObjectType.JWT)
                        .build(),
                claims);

        try {
            signedJWT.sign(new RSASSASigner(rsaKeyManager.privateKey()));
        } catch (JOSEException exception) {
            throw new IllegalStateException("Failed to sign JWT", exception);
        }

        return new AccessToken(signedJWT.serialize(), issuedAt, expiresAt);
    }

    public record AccessToken(String value, Instant issuedAt, Instant expiresAt) {
    }
}
