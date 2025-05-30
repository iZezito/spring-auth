package org.example.springauth.service;

import org.example.springauth.applicationUser.ApplicationUser;
import org.example.springauth.model.auth.TwoFactorAuthentication;
import org.example.springauth.repository.TwoFactorAuthenticationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class TwoFactorAuthService {

    @Autowired
    private TwoFactorAuthenticationRepository twoFactorAuthRepository;

    @Autowired
    private EmailService emailService;

    private static final int CODE_EXPIRATION_MINUTES = 120;

    public void generateAndSend2FACode(ApplicationUser applicationUser) {
        TwoFactorAuthentication twoFactorAuthentication = twoFactorAuthRepository.findByApplicationUser(applicationUser);
        if(twoFactorAuthentication != null) twoFactorAuthRepository.delete(twoFactorAuthentication);
        String code = generateRandomCode();

        TwoFactorAuthentication twoFactorAuth = new TwoFactorAuthentication();
        twoFactorAuth.setApplicationUser(applicationUser);
        twoFactorAuth.setCode(code);
        twoFactorAuth.setExpiryDate(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));

        twoFactorAuthRepository.save(twoFactorAuth);

        String message = "Your two-factor authentication code is: " + code;
        emailService.sendEmail(applicationUser.getEmail(), "Two-Factor Authentication Code", message);
    }

    public boolean validate2FACode(ApplicationUser applicationUser, String code) {
        TwoFactorAuthentication twoFactorAuth = twoFactorAuthRepository.findByApplicationUser(applicationUser);

        boolean valid = twoFactorAuth != null &&
                twoFactorAuth.getCode().equals(code) &&
                twoFactorAuth.getExpiryDate().isAfter(LocalDateTime.now());
        if(valid) twoFactorAuthRepository.delete(twoFactorAuth);
        return valid;
    }

    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
