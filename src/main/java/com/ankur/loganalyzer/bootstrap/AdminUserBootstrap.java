package com.ankur.loganalyzer.bootstrap;

import com.ankur.loganalyzer.config.ApplicationProperties;
import com.ankur.loganalyzer.model.AppUser;
import com.ankur.loganalyzer.model.UserRole;
import com.ankur.loganalyzer.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminUserBootstrap implements ApplicationRunner {

    private static final String DEVELOPMENT_PASSWORD = "change-me";

    private final ApplicationProperties properties;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ApplicationProperties.Security.BootstrapAdmin admin = properties.getSecurity().getBootstrapAdmin();
        if (!admin.isEnabled()) {
            log.info("Admin user bootstrap disabled");
            return;
        }
        if (appUserRepository.existsByUsername(admin.getUsername())) {
            log.info("Admin user '{}' already exists", admin.getUsername());
            return;
        }

        appUserRepository.save(AppUser.builder()
                .username(admin.getUsername())
                .password(passwordEncoder.encode(admin.getPassword()))
                .role(UserRole.ADMIN)
                .enabled(true)
                .build());

        if (DEVELOPMENT_PASSWORD.equals(admin.getPassword())) {
            log.warn("Created default admin user '{}' with development password. Set LOGSPHERE_ADMIN_PASSWORD before production use.",
                    admin.getUsername());
        } else {
            log.info("Created bootstrap admin user '{}'", admin.getUsername());
        }
    }
}
