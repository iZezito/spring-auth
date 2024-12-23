package org.example.springauth.service;

import org.example.springauth.model.auth.EmailVerification;
import org.example.springauth.repository.EmailVerificationRepository;
import org.example.springauth.usuario.Usuario;
import org.example.springauth.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailVerificationService {

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;



    public boolean verifyEmail(String token) {
        EmailVerification emailVerification = emailVerificationRepository.findByVerificationToken(token);

        if (emailVerification != null && emailVerification.getExpiryDate().isAfter(LocalDateTime.now())) {
            Usuario usuario = emailVerification.getUsuario();
            usuario.setEmailVerified(true);
            usuarioRepository.save(usuario);
            emailVerificationRepository.delete(emailVerification);
            return true;
        }

        return false;
    }
}

