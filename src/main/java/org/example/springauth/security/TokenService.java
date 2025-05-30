package org.example.springauth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.example.springauth.applicationUser.ApplicationUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${springauth.security.token.secret}")
    private String secret;

    public String generateToken(ApplicationUser applicationUser) {
        try {
            var algoritmo = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("Auth API")
                    .withSubject(applicationUser.getEmail())
                    .withExpiresAt(expirationDate())
                    .sign(algoritmo);
        } catch (JWTCreationException exception){
            System.out.println("erro ao gerar token jwt");
            return null;
        }
    }

    public String getSubject(String token) {
        try {
         var algoritmo = Algorithm.HMAC256(secret);
            return JWT.require(algoritmo)
                    .withIssuer("Auth API").build()
                    .verify(token)
                    .getSubject();
        } catch (Exception exception) {
            System.out.println("erro ao obter subject do token jwt");
            return null;

        }

    }

    private Instant expirationDate() {
        return LocalDateTime.now().plusDays(4).toInstant(ZoneOffset.of("-03:00"));
    }

}
