package com.example.invitevip.customer.database;

import com.example.invitevip.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByCode(String code);
    Optional<Customer> findByCode(String code);

}
