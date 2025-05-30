package org.example.springauth.controllers;


import org.example.springauth.applicationUser.ApplicationUser;
import org.example.springauth.security.DataTokenJWT;
import org.example.springauth.security.TokenService;
import org.example.springauth.service.TwoFactorAuthService;
import org.example.springauth.applicationUser.AuthenticationCredentials;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/login")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    
    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody @Valid AuthenticationCredentials credentials) {
        try {
            var authenticationToken = new UsernamePasswordAuthenticationToken(credentials.login(), credentials.password());
            var authentication = manager.authenticate(authenticationToken);
            var applicationUser = (ApplicationUser) authentication.getPrincipal();

            if (!applicationUser.isEmailVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email not validated, check your inbox!");
            }

            if (applicationUser.isTwoFactorAuthenticationEnabled()) {
                if (credentials.code() == null || credentials.code().isEmpty()) {
                    twoFactorAuthService.generateAndSend2FACode(applicationUser);
                    return ResponseEntity.accepted().body("Authentication code sent to email.");
                }

                if (!twoFactorAuthService.validate2FACode(applicationUser, credentials.code())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired 2FA code.");
                }
            }

            String tokenJWT = tokenService.generateToken(applicationUser);
            return ResponseEntity.ok(new DataTokenJWT(tokenJWT, applicationUser.getName()));

        } catch (Exception e) {
            logger.error("Authentication failed for user: {}", credentials.login(), e);
            return ResponseEntity.badRequest().body("Invalid credentials. Please verify your email and password and try again.");
        }
    }
}