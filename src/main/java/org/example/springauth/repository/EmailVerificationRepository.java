package org.example.springauth.repository;

import org.example.springauth.generics.GenericRepository;
import org.example.springauth.model.auth.EmailVerification;

public interface EmailVerificationRepository extends GenericRepository<EmailVerification> {
    EmailVerification findByVerificationToken(String token);
}
