package com.example.invitevip.security;

import com.example.invitevip.admin.AdminAuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final AdminAuthorityService adminAuthorityService;

    private final JwtGrantedAuthoritiesConverter scopeAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        Collection<GrantedAuthority> scopeAuthorities =
                (Collection<GrantedAuthority>) scopeAuthoritiesConverter.convert(jwt);

        if (scopeAuthorities != null) {
            authorities.addAll(scopeAuthorities);
        }

        authorities.addAll(extractRealmRoles(jwt));
        authorities.addAll(adminAuthorityService.loadAuthorities(jwt));

        String principalName = jwt.getClaimAsString("preferred_username");
        if (principalName == null || principalName.isBlank()) {
            principalName = jwt.getSubject();
        }

        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    private Set<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Set<GrantedAuthority> roles = new HashSet<>();

        Object realmAccessObject = jwt.getClaims().get("realm_access");
        if (!(realmAccessObject instanceof Map<?, ?> realmAccess)) {
            return roles;
        }

        Object rolesObject = realmAccess.get("roles");
        if (!(rolesObject instanceof Collection<?> roleList)) {
            return roles;
        }

        for (Object role : roleList) {
            if (role instanceof String roleName && !roleName.isBlank()) {
                roles.add(new SimpleGrantedAuthority("ROLE_" + roleName.trim().toUpperCase()));
            }
        }

        return roles;
    }
}