package com.example.invitevip.admin;

import com.example.invitevip.admin.database.AdminRepository;
import com.example.invitevip.admin.entity.Admin;
import com.example.invitevip.admin.entity.AdminPermission;
import com.example.invitevip.admin.entity.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminAuthorityService {

    private static final String CUSTOMER_READ = "CUSTOMER_READ";

    private final AdminRepository adminRepository;

    public Set<GrantedAuthority> loadAuthorities(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        String preferredUsername = jwt.getClaimAsString("preferred_username");

        Optional<Admin> optionalAdmin = Optional.empty();

        if (keycloakId != null && !keycloakId.isBlank()) {
            optionalAdmin = adminRepository.findByKeycloakId(keycloakId);
        }

        if (optionalAdmin.isEmpty() && preferredUsername != null && !preferredUsername.isBlank()) {
            optionalAdmin = adminRepository.findByUsername(preferredUsername);
        }

        if (optionalAdmin.isEmpty()) {
            return Set.of();
        }

        Admin admin = optionalAdmin.get();

        if ((admin.getKeycloakId() == null || admin.getKeycloakId().isBlank())
                && keycloakId != null && !keycloakId.isBlank()) {
            admin.setKeycloakId(keycloakId);
        }

        Set<GrantedAuthority> authorities = new HashSet<>();

        if (admin.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority(
                    "ROLE_" + admin.getRole().name()
            ));
        }

        for (AdminPermission adminPermission : admin.getAdminPermissions()) {
            Permission permission = adminPermission.getPermission();

            if (permission != null
                    && permission.getCode() != null
                    && !permission.getCode().isBlank()
                    && !CUSTOMER_READ.equals(permission.getCode().trim().toUpperCase())) {
                authorities.add(new SimpleGrantedAuthority(
                        permission.getCode().trim().toUpperCase()
                ));
            }
        }

        return authorities;
    }
}
