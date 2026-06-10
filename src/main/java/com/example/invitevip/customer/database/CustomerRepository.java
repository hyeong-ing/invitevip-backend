package com.example.invitevip.customer.database;

import com.example.invitevip.customer.entity.Customer;
import com.example.invitevip.customer.entity.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByInviteCode(InviteCode inviteCode);
    Optional<Customer> findByInviteCode(InviteCode inviteCode);

}
