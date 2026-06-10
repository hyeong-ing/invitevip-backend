package com.example.invitevip.admin;

import com.example.invitevip.admin.database.AdminRepository;
import com.example.invitevip.admin.database.PermissionRepository;
import com.example.invitevip.admin.dto.AdminRequest;
import com.example.invitevip.admin.dto.AdminResponse;
import com.example.invitevip.admin.entity.Admin;
import com.example.invitevip.admin.entity.AdminPermission;
import com.example.invitevip.admin.entity.AdminRole;
import com.example.invitevip.admin.entity.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminRepository adminRepository;
    private final PermissionRepository permissionRepository;

    private final Keycloak keycloakAdminClient;
    private final String REALM_NAME = "invitevip";

    public List<AdminResponse> findAllAdmins() {
        return adminRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public boolean exists(Long id) {
        return adminRepository.existsById(id);
    }

    @Transactional
    public AdminResponse save(AdminRequest request) {
        validateDuplicateUsername(null, request.getUsername());

        Admin admin = new Admin();
        admin.setName(request.getName());
        admin.setUsername(request.getUsername());
        admin.setRole(resolveRole(request.getRole()));

        replacePermissions(admin, request.getPermissions());

        Admin savedAdmin = adminRepository.saveAndFlush(admin);

        String keycloakId = createKeycloakUser(request);
        savedAdmin.setKeycloakId(keycloakId);
        adminRepository.flush();

        return toResponse(savedAdmin);
    }

    @Transactional
    public AdminResponse update(Long id, AdminRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        validateDuplicateUsername(id, request.getUsername());

        String oldUsername = admin.getUsername();

        admin.setName(request.getName());
        admin.setUsername(request.getUsername());
        admin.setRole(resolveRole(request.getRole()));

        replacePermissions(admin, request.getPermissions());

        adminRepository.flush();
        updateKeycloakUser(admin, oldUsername, request);

        return toResponse(admin);
    }

    @Transactional
    public void delete(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        String keycloakId = getKeycloakId(admin, admin.getUsername());

        adminRepository.delete(admin);
        adminRepository.flush();

        deleteKeycloakUser(keycloakId);
    }

    public List<AdminResponse> searchAdmins(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isBlank()) {
            return List.of();
        }

        return adminRepository.findDistinctByNameContainingOrUsernameContaining(normalizedKeyword, normalizedKeyword).stream()
                .map(this::toResponse)
                .toList();
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private String createKeycloakUser(AdminRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setFirstName(request.getName());
        user.setEnabled(true);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.getPassword());
            credential.setTemporary(false);

            user.setCredentials(List.of(credential));
        }

        Response response = keycloakAdminClient.realm(REALM_NAME).users().create(user);

        try {
            if (response.getStatus() != 201) {
                throw new RuntimeException("Keycloak 사용자 생성 실패 (아이디 중복 등): HTTP 상태 코드 " + response.getStatus());
            }

            return extractCreatedUserId(response)
                    .or(() -> findKeycloakUserIdByUsername(request.getUsername()))
                    .orElseThrow(() -> new IllegalStateException("생성된 Keycloak 사용자 ID를 찾을 수 없습니다."));
        } finally {
            response.close();
        }
    }

    private Optional<String> extractCreatedUserId(Response response) {
        URI location = response.getLocation();
        if (location == null || location.getPath() == null) {
            return Optional.empty();
        }

        String path = location.getPath();
        String userId = path.substring(path.lastIndexOf('/') + 1);
        return userId.isBlank() ? Optional.empty() : Optional.of(userId);
    }

    private void updateKeycloakUser(Admin admin, String oldUsername, AdminRequest request) {
        String keycloakId = getKeycloakId(admin, oldUsername);

        UserRepresentation user = keycloakAdminClient.realm(REALM_NAME).users().get(keycloakId).toRepresentation();
        user.setUsername(request.getUsername());
        user.setFirstName(request.getName());
        user.setEnabled(true);

        keycloakAdminClient.realm(REALM_NAME).users().get(keycloakId).update(user);

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            updateKeycloakPassword(keycloakId, request.getPassword());
        }
    }

    private void updateKeycloakPassword(String keycloakId, String newPassword) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);
        credential.setTemporary(false);

        keycloakAdminClient.realm(REALM_NAME).users().get(keycloakId).resetPassword(credential);
    }

    private void deleteKeycloakUser(String keycloakId) {
        keycloakAdminClient.realm(REALM_NAME).users().get(keycloakId).remove();
    }

    private String getKeycloakId(Admin admin, String usernameFallback) {
        if (admin.getKeycloakId() != null && !admin.getKeycloakId().isBlank()) {
            return admin.getKeycloakId();
        }

        String keycloakId = findKeycloakUserIdByUsername(usernameFallback)
                .orElseThrow(() -> new IllegalArgumentException("Keycloak에서 해당 사용자를 찾을 수 없습니다."));

        admin.setKeycloakId(keycloakId);
        return keycloakId;
    }

    private Optional<String> findKeycloakUserIdByUsername(String username) {
        List<UserRepresentation> users = keycloakAdminClient.realm(REALM_NAME).users().search(username, true);
        return users.stream()
                .filter(user -> username.equals(user.getUsername()))
                .map(UserRepresentation::getId)
                .filter(Objects::nonNull)
                .findFirst();
    }

    private void validateDuplicateUsername(Long id, String username) {
        boolean exists = false;
        if (id == null) {
            exists = adminRepository.existsByUsername(username);
        } else {
            Admin existing = adminRepository.findByUsername(username).orElse(null);
            if (existing != null && !existing.getId().equals(id)) {
                exists = true;
            }
        }
        if (exists) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }
    }

    private AdminRole resolveRole(String roleName) {
        try {
            return AdminRole.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            return AdminRole.ADMIN;
        }
    }

    private void replacePermissions(Admin admin, List<String> permissionCodes) {
        Set<String> requestedCodes = permissionCodes == null
                ? Set.of()
                : permissionCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (requestedCodes.isEmpty()) {
            admin.getAdminPermissions().clear();
            return;
        }

        List<Permission> permissions = permissionRepository.findByCodeIn(List.copyOf(requestedCodes));

        Set<String> foundCodes = permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        List<String> missingCodes = requestedCodes.stream()
                .filter(code -> !foundCodes.contains(code))
                .toList();

        if (!missingCodes.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 권한 코드입니다: " + String.join(", ", missingCodes));
        }

        admin.getAdminPermissions().removeIf(adminPermission -> {
            Permission permission = adminPermission.getPermission();
            return permission == null
                    || permission.getCode() == null
                    || !requestedCodes.contains(permission.getCode());
        });

        Set<String> existingCodes = admin.getAdminPermissions().stream()
                .map(AdminPermission::getPermission)
                .filter(Objects::nonNull)
                .map(Permission::getCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Permission permission : permissions) {
            if (existingCodes.contains(permission.getCode())) {
                continue;
            }

            AdminPermission adminPermission = new AdminPermission();
            adminPermission.setAdmin(admin);
            adminPermission.setPermission(permission);
            admin.getAdminPermissions().add(adminPermission);
        }
    }

    private AdminResponse toResponse(Admin admin) {
        AdminResponse response = new AdminResponse();
        response.setId(admin.getId());
        response.setName(admin.getName());
        response.setUsername(admin.getUsername());
        response.setRole(admin.getRole().name());

        List<String> permissionCodes = admin.getAdminPermissions().stream()
                .map(AdminPermission::getPermission)
                .filter(Objects::nonNull)
                .map(Permission::getCode)
                .filter(Objects::nonNull)
                .sorted(Comparator.naturalOrder())
                .toList();

        response.setPermissions(permissionCodes);
        return response;
    }
}
