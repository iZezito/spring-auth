package org.example.springauth.controllers;

import lombok.extern.slf4j.Slf4j;
import org.example.springauth.security.TokenService;
import org.example.springauth.usuario.Usuario;
import org.example.springauth.usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2/login")
@Slf4j
public class OauthController {
    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/success")
    public ResponseEntity<?> loginSuccess(Authentication authentication) {
        if (authentication == null || !(authentication instanceof OAuth2AuthenticationToken oauth2Auth)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro na autenticação OAuth2");
        }

        try {
            OAuth2User oauth2User = oauth2Auth.getPrincipal();
            String registrationId = oauth2Auth.getAuthorizedClientRegistrationId();

            String email = oauth2User.getAttribute("email");
            String login = oauth2User.getAttribute("login");
            String name = oauth2User.getAttribute("name");


            String emailFinal;
            String nomeFinal;

            if ("github".equals(registrationId) && email == null) {
                emailFinal = login + "@github.com";
                nomeFinal = (name != null) ? name : login;
            } else {
                emailFinal = email;
                nomeFinal = name;
            }

            if (emailFinal == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email não encontrado no OAuth2");
            }

            final String finalEmail = emailFinal;
            final String finalNome = nomeFinal;

            Usuario usuario = usuarioService.findByEmail(finalEmail)
                    .orElseGet(() -> usuarioService.criarNovoUsuario(finalEmail, finalNome, registrationId));

            String tokenJWT = tokenService.gerarToken(usuario);

            String redirectUrl = "http://localhost:4200/oauth-success?token=" + tokenJWT;

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar autenticação: " + e.getMessage());
        }
    }
}

