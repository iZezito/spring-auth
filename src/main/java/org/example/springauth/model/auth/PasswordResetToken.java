package org.example.springauth.model.auth;

import org.example.springauth.applicationUser.ApplicationUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private ApplicationUser user;

    private LocalDateTime expiryDate;

    public PasswordResetToken(String token, ApplicationUser user) {
        this.token = token;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusHours(1);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }


}
