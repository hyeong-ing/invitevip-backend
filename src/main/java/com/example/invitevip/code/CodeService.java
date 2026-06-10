package com.example.invitevip.code;

import com.example.invitevip.customer.database.CustomerRepository;
import com.example.invitevip.customer.dto.CustomerResponse;
import com.example.invitevip.customer.CustomerService;
import com.example.invitevip.customer.entity.InviteCode;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CodeService {
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    public CodeService(CustomerRepository customerRepository, CustomerService customerService) {
        this.customerRepository = customerRepository;
        this.customerService = customerService;
    }

    public Optional<CustomerResponse> findByCode(String code) {
        if (!InviteCode.isValid(code)) {
            return Optional.empty();
        }

        return customerRepository.findByInviteCode(InviteCode.of(code))
                .map(customerService::toResponse);
    }
}
