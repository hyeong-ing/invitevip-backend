package com.example.invitevip.security;

import com.example.invitevip.admin.dto.AuthMeResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public AuthMeResponse me(Authentication authentication) {
        Set<String> authorities = new TreeSet<>();

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            authorities.add(authority.getAuthority());
        }

        boolean superAdmin = authorities.contains("ROLE_SUPER_ADMIN");

        return AuthMeResponse.builder()
                .username(authentication.getName())
                .authorities(List.copyOf(authorities))
                .superAdmin(superAdmin)
                .customerRead(superAdmin || authorities.contains("CUSTOMER_READ"))
                .customerSearch(superAdmin || authorities.contains("CUSTOMER_SEARCH"))
                .customerAdd(superAdmin || authorities.contains("CUSTOMER_ADD"))
                .customerEdit(superAdmin || authorities.contains("CUSTOMER_EDIT"))
                .customerDelete(superAdmin || authorities.contains("CUSTOMER_DELETE"))
                .build();
    }
}
