package com.mediapp.security_service.controller;

import com.mediapp.security_service.config.RsaKeyManager;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JwksController {

    private final RsaKeyManager rsaKeyManager;

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getJwks() {
        return rsaKeyManager.jwkSet().toJSONObject();
    }
}
