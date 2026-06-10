package com.example.invitevip.admin.database;

import com.example.invitevip.admin.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByCodeIn(List<String> codes);
}
