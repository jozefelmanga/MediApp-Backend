package com.mediapp.security_service.bootstrap;

import com.mediapp.security_service.config.properties.AdminBootstrapProperties;
import com.mediapp.security_service.service.AppUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

    private final AppUserService appUserService;
    private final AdminBootstrapProperties adminBootstrapProperties;

    @Override
    public void run(ApplicationArguments args) {
        boolean created = appUserService.ensureAdminUser(adminBootstrapProperties);
        if (created) {
            log.info("Default admin credentials seeded for email={}. Ensure the password is rotated.",
                    adminBootstrapProperties.getEmail());
        }
    }
}
