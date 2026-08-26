package com.proxymanager.web;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proxymanager.repository.UserRepository;
import com.proxymanager.security.JwtService;
import com.proxymanager.web.dto.LoginRequest;
import com.proxymanager.web.dto.LoginResponse;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * /auth/** is intentionally left open in SecurityConfig - you can't be required to
 * already hold a token in order to obtain one.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Mono.fromCallable(() -> userRepository.findByUsername(request.username()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty)
                .filter(user -> passwordEncoder.matches(request.password(), user.getPasswordHash()))
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password")))
                .map(user -> new LoginResponse(jwtService.generateToken(user.getUsername())));
    }
}
