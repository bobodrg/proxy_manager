package com.proxymanager.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.proxymanager.domain.User;
import com.proxymanager.repository.UserRepository;

/**
 * Creates a default admin user on startup if the app_user table is empty, so there's
 * always a way to log in without a manual SQL insert.
 *
 * Unlike the request-handling code elsewhere in this app, this class is allowed to call
 * the (blocking) JPA repository directly: ApplicationRunner beans run once, synchronously,
 * on the main thread during application startup - before Netty starts accepting requests
 * on the event loop. There's no event loop to block yet, so no Schedulers.boundedElastic()
 * wrapping is needed here.
 */
@Component
public class AdminUserSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultUsername;
    private final String defaultPassword;

    public AdminUserSeeder(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${app.admin.default-username}") String defaultUsername,
                            @Value("${app.admin.default-password}") String defaultPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultUsername = defaultUsername;
        this.defaultPassword = defaultPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() == 0) {
            User admin = new User(defaultUsername, passwordEncoder.encode(defaultPassword));
            userRepository.save(admin);
        }
    }
}
