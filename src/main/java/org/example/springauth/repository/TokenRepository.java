package org.example.springauth.repository;

import org.example.springauth.generics.GenericRepository;
import org.example.springauth.model.auth.PasswordResetToken;

import java.util.Optional;

public interface TokenRepository extends GenericRepository<PasswordResetToken> {

    Optional<PasswordResetToken> findByToken(String token);
}
