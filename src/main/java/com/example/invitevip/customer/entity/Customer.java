package com.example.invitevip.customer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customer", uniqueConstraints = @UniqueConstraint(name = "uk_customer_code", columnNames = "code"))
@Getter
@Setter
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5)
    private String name;

    @Column(nullable = false, length = 10)
    private String grade;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 4)
    private String code;

    @Column(length = 255)
    private String note;
}
