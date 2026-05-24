package com.example.invitevip.admin;

import com.example.invitevip.admin.dto.AdminRequest;
import com.example.invitevip.admin.dto.AdminResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public List<AdminResponse> findAllAdmins() {
        return adminService.findAllAdmins();
    }

    @PostMapping
    public ResponseEntity<?> saveAdmin(@RequestBody AdminRequest request) {
        try {
            AdminResponse savedAdmin = adminService.save(request);
            return ResponseEntity.ok(savedAdmin);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long id, @RequestBody AdminRequest request) {
        if (!adminService.exists(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            AdminResponse updatedAdmin = adminService.update(id, request);
            return ResponseEntity.ok(updatedAdmin);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAdmin(@PathVariable Long id) {

        if (!adminService.exists(id)) {
            return ResponseEntity.notFound().build();
        }

        adminService.delete(id);
        return ResponseEntity.ok("관리자가 삭제되었습니다.");
    }

    @GetMapping("/search")
    public List<AdminResponse> searchAdmins(@RequestParam String keyword) {
        return adminService.searchAdmins(keyword);
    }
}