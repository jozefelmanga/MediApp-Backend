package com.mediapp.security_service.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Lazily produces an RSA key pair for signing JWT access tokens and exposes its
 * public portion for JWKS publication.
 */
@Component
public class RsaKeyManager {

    private final RSAKey rsaKey;
    private final JWKSet jwkSet;

    public RsaKeyManager() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            String keyId = UUID.randomUUID().toString();
            this.rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(keyId)
                    .build();
            this.jwkSet = new JWKSet(rsaKey.toPublicJWK());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Unable to initialize RSA key pair", exception);
        }
    }

    public RSAPublicKey publicKey() {
        try {
            return rsaKey.toRSAPublicKey();
        } catch (JOSEException e) {
            throw new IllegalStateException("Unable to retrieve RSA public key", e);
        }
    }

    public RSAPrivateKey privateKey() {
        try {
            return rsaKey.toRSAPrivateKey();
        } catch (JOSEException e) {
            throw new IllegalStateException("Unable to retrieve RSA private key", e);
        }
    }

    public String keyId() {
        return rsaKey.getKeyID();
    }

    public JWKSet jwkSet() {
        return jwkSet;
    }
}
