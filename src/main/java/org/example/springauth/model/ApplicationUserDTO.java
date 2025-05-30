package org.example.springauth.model;

import org.example.springauth.applicationUser.ApplicationUser;
import lombok.Data;

import java.util.List;

@Data
public class ApplicationUserDTO {
    private Long id;
    private String name;
    private String email;
    private boolean twoFactorAuthenticationEnabled;

    public static List<ApplicationUserDTO> convert(List<ApplicationUser> applicationUsers) {
        return applicationUsers.stream()
                .map(ApplicationUserDTO::convertToUserDTO)
                .toList();
    }

    public static ApplicationUserDTO convertToUserDTO(ApplicationUser applicationUser) {
        ApplicationUserDTO dto = new ApplicationUserDTO();
        dto.setId(applicationUser.getId());
        dto.setName(applicationUser.getName());
        dto.setEmail(applicationUser.getEmail());
        dto.setTwoFactorAuthenticationEnabled(applicationUser.isTwoFactorAuthenticationEnabled());

        return dto;
    }
}
