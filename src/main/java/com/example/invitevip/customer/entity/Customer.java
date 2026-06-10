package com.example.invitevip.customer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer", uniqueConstraints = @UniqueConstraint(name = "uk_customer_code", columnNames = "code"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "code", nullable = false, length = 4))
    private InviteCode inviteCode;

    @Column(length = 255)
    private String note;

    public static Customer create(String name, String grade, String phone, InviteCode inviteCode, String note) {
        Customer customer = new Customer();
        customer.update(name, grade, phone, inviteCode, note);
        return customer;
    }

    public void update(String name, String grade, String phone, InviteCode inviteCode, String note) {
        this.name = name;
        this.grade = grade;
        this.phone = phone;
        this.inviteCode = inviteCode;
        this.note = note;
    }

    public String getCode() {
        return inviteCode.getValue();
    }
}
