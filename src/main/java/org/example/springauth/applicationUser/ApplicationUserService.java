package org.example.springauth.applicationUser;

import org.example.springauth.model.auth.EmailVerification;
import org.example.springauth.model.auth.PasswordResetToken;
import org.example.springauth.repository.EmailVerificationRepository;
import org.example.springauth.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApplicationUserService {
    @Autowired
    private ApplicationUserRepository repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    private static final int EXPIRATION_HOURS = 24;

    public ApplicationUser save(ApplicationUser applicationUser){
        applicationUser.setPassword(encoder.encode(applicationUser.getPassword()));
        applicationUser.setEmailVerified(false);
        applicationUser.setOauth2Provider(null);
        return repository.save(applicationUser);

    }

    public void update(ApplicationUser applicationUser) {
        repository.saveAndFlush(applicationUser);
    }

    public ApplicationUser findByLogin(String login){
        return (ApplicationUser) repository.findByEmail(login);
    }

    public ApplicationUser getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public String createPasswordResetToken(ApplicationUser user) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
        tokenRepository.save(passwordResetToken);
        return token;
    }

    public String createVerificationEmailToken(ApplicationUser user) {
        String verificationToken = UUID.randomUUID().toString();

        EmailVerification emailVerification = new EmailVerification();
        emailVerification.setApplicationUser(user);
        emailVerification.setVerificationToken(verificationToken);
        emailVerification.setExpiryDate(LocalDateTime.now().plusHours(EXPIRATION_HOURS));

        emailVerificationRepository.save(emailVerification);

        return verificationToken;
    }

    public void updatePassword(ApplicationUser user, String newPassword) {
        user.setPassword(encoder.encode(newPassword));
        repository.save(user);
    }

    public Optional<ApplicationUser> findByEmail(String email) {
        return Optional.ofNullable((ApplicationUser) repository.findByEmail(email));
    }

    public ApplicationUser createNewApplicationUser(String email, String nome, String provider) {
        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setEmail(email);
        applicationUser.setName(nome);
        applicationUser.setEmailVerified(true);
        applicationUser.setOauth2Provider(provider);

        return repository.save(applicationUser);
    }
}
