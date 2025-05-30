package org.example.springauth.controllers;


import org.example.springauth.applicationUser.ApplicationUser;
import org.example.springauth.model.ApplicationUserDTO;
import org.example.springauth.model.auth.PasswordResetToken;
import org.example.springauth.repository.TokenRepository;
import org.example.springauth.service.EmailService;
import org.example.springauth.service.EmailVerificationService;
import org.example.springauth.applicationUser.ApplicationUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class ApplicationUserController {


    @Autowired
    private ApplicationUserService service;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @PostMapping
    public ResponseEntity<String> insert(@RequestBody @Valid ApplicationUser applicationUser){
        ApplicationUser user = service.save(applicationUser);
        StringBuilder builder = new StringBuilder();
        String token = service.createVerificationEmailToken(user);
        emailService.sendEmail(user.getEmail(), "Account Verification", builder.append("Click on the link to validate your email: ").append("http://localhost:5173/validate-email?token=").append(token).toString());
        return ResponseEntity.ok("Registered user. Check your email for activation.");
    }

    @GetMapping("/")
    public ResponseEntity<ApplicationUserDTO> getLoggedUser(Authentication authentication) {
        ApplicationUser applicationUser = (ApplicationUser) authentication.getPrincipal();
        ApplicationUserDTO applicationUserDTO = ApplicationUserDTO.convertToUserDTO(applicationUser);
        return ResponseEntity.ok(applicationUserDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationUserDTO> update(@RequestBody ApplicationUserDTO applicationUser,
                                                     @PathVariable Long id,
                                                     Authentication authentication) {
        ApplicationUser applicationUserLogged = (ApplicationUser) authentication.getPrincipal();

        if (!applicationUserLogged.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ApplicationUser applicationUserBanco = service.getById(id);
        if (applicationUserBanco == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        applicationUserBanco.setName(applicationUser.getName());
        applicationUserBanco.setEmail(applicationUser.getEmail());
        applicationUserBanco.setTwoFactorAuthenticationEnabled(applicationUser.isTwoFactorAuthenticationEnabled());

        service.update(applicationUserBanco);

        ApplicationUserDTO applicationUserDTO = ApplicationUserDTO.convertToUserDTO(applicationUserBanco);
        return ResponseEntity.ok(applicationUserDTO);
    }

    @GetMapping("/login/{login}")
    public Boolean login(@PathVariable String login){
        ApplicationUser applicationUserBanco = service.findByLogin(login);
        return applicationUserBanco != null;
    }

    @GetMapping("/{id}")
    public ApplicationUser findById(@PathVariable Long id){
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
        Optional<ApplicationUser> userOpt = Optional.ofNullable(service.findByLogin(email));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }
        StringBuilder builder = new StringBuilder();
        ApplicationUser user = userOpt.get();
        String token = service.createPasswordResetToken(user);
        emailService.sendEmail(user.getEmail(), "Redefinição de senha", builder.append("Clique no link para redefinir sua senha: ").append("http://localhost:5173/reset-password?token=").append(token).toString());

        return ResponseEntity.ok("E-mail de recuperação de senha enviado.");
    }

    @PostMapping("/password-reset")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        Optional<PasswordResetToken> resetTokenOpt = tokenRepository.findByToken(token);
        if (resetTokenOpt.isEmpty() || resetTokenOpt.get().isExpired()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido ou expirado.");
        }

        ApplicationUser user = resetTokenOpt.get().getUser();
        service.updatePassword(user, newPassword);
        tokenRepository.delete(resetTokenOpt.get());

        return ResponseEntity.ok("Senha alterada com sucesso.");
    }





}
