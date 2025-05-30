package org.example.springauth.service;

import org.example.springauth.applicationUser.ApplicationUser;
import org.example.springauth.model.auth.EmailVerification;
import org.example.springauth.repository.EmailVerificationRepository;
import org.example.springauth.applicationUser.ApplicationUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailVerificationService {

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;



    public boolean verifyEmail(String token) {
        EmailVerification emailVerification = emailVerificationRepository.findByVerificationToken(token);

        if (emailVerification != null && emailVerification.getExpiryDate().isAfter(LocalDateTime.now())) {
            ApplicationUser applicationUser = emailVerification.getApplicationUser();
            applicationUser.setEmailVerified(true);

            applicationUserRepository.save(applicationUser);
            emailVerificationRepository.delete(emailVerification);

            return true;
        }

        return false;
    }
}

