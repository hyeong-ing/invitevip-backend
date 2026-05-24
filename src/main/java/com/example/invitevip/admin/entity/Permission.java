package com.example.invitevip.admin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permission", uniqueConstraints = @UniqueConstraint(name = "uk_permission_code", columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private  String code;

    @Column(nullable = false, length = 50)
    private  String name;

}
