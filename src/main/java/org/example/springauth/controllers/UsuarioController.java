package org.example.springauth.controllers;


import org.example.springauth.model.UsuarioCreateDTO;
import org.example.springauth.model.UsuarioDTO;
import org.example.springauth.model.auth.PasswordResetToken;
import org.example.springauth.repository.TokenRepository;
import org.example.springauth.service.EmailService;
import org.example.springauth.service.EmailVerificationService;
import org.example.springauth.usuario.Usuario;
import org.example.springauth.usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {


    @Autowired
    private UsuarioService service;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @PostMapping
    public ResponseEntity<String> insert(@RequestBody @Valid Usuario usuario){
        System.out.println("Senha:" + usuario.getPassword());
        Usuario user = service.save(usuario);
        StringBuilder builder = new StringBuilder();
        String token = service.createVerificatioEmailToken(user);
        emailService.sendEmail(user.getEmail(), "Verificação de Conta", builder.append("Clique no link para validar seu email: ").append("http://localhost:5173/validate-email?token=").append(token).toString());
        return ResponseEntity.ok("Usuário registrado. Verifique seu e-mail para ativação.");
    }

    @GetMapping("/")
    public ResponseEntity<UsuarioDTO> getUsuarioLogado(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        UsuarioDTO usuarioDTO = UsuarioDTO.convertToUsuarioDTO(usuario);
        return ResponseEntity.ok(usuarioDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> update(@RequestBody UsuarioCreateDTO usuario,
                                             @PathVariable Long id,
                                             Authentication authentication) {
        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();

        if (!usuarioLogado.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Usuario usuarioBanco = service.getById(id);
        if (usuarioBanco == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        usuarioBanco.setNome(usuario.getNome());
        usuarioBanco.setEmail(usuario.getEmail());
        usuarioBanco.setTwoFactorAuthenticationEnabled(usuario.isTwoFactorAuthenticationEnabled());

        service.update(usuarioBanco);

        UsuarioDTO usuarioDTO = UsuarioDTO.convertToUsuarioDTO(usuarioBanco);
        return ResponseEntity.ok(usuarioDTO);
    }

    @GetMapping("/login/{login}")
    public Boolean login(@PathVariable String login){
        Usuario usuarioBanco = service.findByLogin(login);
        return usuarioBanco != null;
    }

    @GetMapping("/{id}")
    public Usuario findById(@PathVariable Long id){
        return service.getById(id);
    }


    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        if (emailVerificationService.verifyEmail(token)) {
            return ResponseEntity.ok("E-mail verificado com sucesso!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token de verificação inválido ou expirado.");
        }
    }



    @PostMapping("/password-reset-tk")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) {
        Optional<Usuario> userOpt = Optional.ofNullable(service.findByLogin(email));
        userOpt.ifPresent(user -> {
            StringBuilder builder = new StringBuilder();
            String token = service.createPasswordResetToken(user);
            emailService.sendEmail(user.getEmail(), "Redefinição de senha", builder.append("Clique no link para redefinir sua senha: ").append("http://localhost:5173/reset-password?token=").append(token).toString());
        });
        return ResponseEntity.ok("Se existir uma conta com esse e-mail cadastrado, ela receberá um e-mail com instruções para redefinição de senha.");
    }

    @PostMapping("/password-reset")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        Optional<PasswordResetToken> resetTokenOpt = tokenRepository.findByToken(token);

        if (resetTokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token não encontrado.");
        }

        PasswordResetToken resetToken = resetTokenOpt.get();

        if (resetToken.isExpired()) {
            return ResponseEntity.status(HttpStatus.GONE).body("Token expirado.");
        }

        Usuario user = resetToken.getUser();
        service.updatePassword(user, newPassword);
        tokenRepository.delete(resetToken);

        return ResponseEntity.ok("Senha alterada com sucesso.");
    }






}
