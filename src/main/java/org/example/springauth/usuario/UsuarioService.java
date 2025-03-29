package org.example.springauth.usuario;

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
public class UsuarioService {
    @Autowired
    private UsuarioRepository repository;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    private static final int EXPIRATION_HOURS = 24;

    public Usuario save(Usuario usuario){
        usuario.setPassword(encoder.encode(usuario.getPassword()));
        usuario.setEmailVerified(false);
        usuario.setOauth2Provider(null);
        return repository.save(usuario);

    }

    public void update(Usuario usuario) {
        repository.saveAndFlush(usuario);
    }

    public Usuario findByLogin(String login){
        return (Usuario) repository.findByEmail(login);
    }

    public Usuario getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public String createPasswordResetToken(Usuario user) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
        tokenRepository.save(passwordResetToken);
        return token;
    }

    public String createVerificatioEmailToken(Usuario user) {
        String verificationToken = UUID.randomUUID().toString();

        EmailVerification emailVerification = new EmailVerification();
        emailVerification.setUsuario(user);
        emailVerification.setVerificationToken(verificationToken);
        emailVerification.setExpiryDate(LocalDateTime.now().plusHours(EXPIRATION_HOURS));

        emailVerificationRepository.save(emailVerification);

        return verificationToken;
    }

    public void updatePassword(Usuario user, String newPassword) {
        user.setPassword(encoder.encode(newPassword));
        repository.save(user);
    }

    public Optional<Usuario> findByEmail(String email) {
        return Optional.ofNullable((Usuario) repository.findByEmail(email));
    }

    public Usuario criarNovoUsuario(String email, String nome) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setNome(nome);
        usuario.setEmailVerified(true);
        usuario.setOauth2Provider("GOOGLE");
        return repository.save(usuario);
    }
}
