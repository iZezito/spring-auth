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
@Slf4j  // Adicione esta anotação se quiser usar o logger
public class GoogleOAuthController {
    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/success")
    public ResponseEntity<?> loginSuccess(Authentication authentication) {
        log.info("Recebendo requisição de login success"); // Log para debug

        if (authentication == null) {
            log.error("Authentication é null");  // Log para debug
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro na autenticação com o Google");
        }

        try {
            OAuth2AuthenticationToken oauth2Auth = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Auth.getPrincipal();

            log.info("Usuário OAuth2 autenticado: {}", oauth2User.getName());  // Log para debug

            String email = oauth2User.getAttribute("email");
            String nome = oauth2User.getAttribute("name");

            if (email == null) {
                log.error("Email não encontrado nos atributos do OAuth2User");  // Log para debug
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email não encontrado");
            }

            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseGet(() -> usuarioService.criarNovoUsuario(email, nome));

            String tokenJWT = tokenService.gerarToken(usuario);

            // Redireciona para o frontend com o token JWT
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "http://localhost:5173/oauth-success?token=" + tokenJWT)
                    .build();

        } catch (Exception e) {
            log.error("Erro ao processar autenticação OAuth2", e);  // Log para debug
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar autenticação: " + e.getMessage());
        }
    }
}

