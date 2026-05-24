package com.example.invitevip.code;

import com.example.invitevip.customer.database.CustomerRepository;
import com.example.invitevip.customer.entity.Customer;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CodeService {
    private final CustomerRepository customerRepository;

    public CodeService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Optional<Customer> findByCode(String code) {
        return customerRepository.findByCode(code);
    }
}
