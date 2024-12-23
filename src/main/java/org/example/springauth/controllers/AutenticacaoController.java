package org.example.springauth.controllers;


import org.example.springauth.security.DadosTokenJWT;
import org.example.springauth.security.TokenService;
import org.example.springauth.service.TwoFactorAuthService;
import org.example.springauth.usuario.DadosAutenticacao;
import org.example.springauth.usuario.Usuario;
import org.example.springauth.usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/login")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<?> efetuarLogin(@RequestBody @Valid DadosAutenticacao dados) {
        try {
            var authenticationToken = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());
            var authentication = manager.authenticate(authenticationToken);
            var usuario = (Usuario) authentication.getPrincipal();

            if (!usuario.isEmailVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("E-mail não validado, cheque sua caixa de entrada!");
            }

            if (usuario.isTwoFactorAuthenticationEnabled()) {
                twoFactorAuthService.generateAndSend2FACode(usuario);
                return ResponseEntity.accepted().body("Código de autenticação enviado para o e-mail.");
            } else {
                String tokenJWT = tokenService.gerarToken(usuario);
                return ResponseEntity.ok(new DadosTokenJWT(tokenJWT, usuario.getNome()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Credenciais inválidas. Por favor, verifique seu e-mail e senha e tente novamente.");
        }
    }

    @PostMapping("/validate-2fa")
    public ResponseEntity<?> validateTwoFactor(@RequestParam String username, @RequestParam String code) {
        try {
            Optional<Usuario> userOpt = Optional.ofNullable(usuarioService.findByLogin(username));
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
            }
            Usuario usuario = userOpt.get();
            if (twoFactorAuthService.validate2FACode(usuario, code)) {
                String tokenJWT = tokenService.gerarToken(usuario);
                return ResponseEntity.ok(new DadosTokenJWT(tokenJWT, usuario.getNome()));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Código 2FA inválido ou expirado.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao validar 2FA.");
        }
    }

}
