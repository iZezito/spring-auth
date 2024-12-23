package org.example.springauth.repository;

import org.example.springauth.generics.GenericRepository;
import org.example.springauth.model.auth.TwoFactorAuthentication;
import org.example.springauth.usuario.Usuario;

public interface TwoFactorAuthenticationRepository extends GenericRepository<TwoFactorAuthentication> {
    TwoFactorAuthentication findByUsuario(Usuario usuario);
}
