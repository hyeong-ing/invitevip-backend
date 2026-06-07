package com.example.invitevip.customer;

import com.example.invitevip.customer.entity.Customer;
import com.example.invitevip.customer.entity.InviteCode;
import com.example.invitevip.customer.database.CustomerRepository;
import com.example.invitevip.customer.database.CustomerSearchRepository;
import com.example.invitevip.customer.dto.CustomerRequest;
import com.example.invitevip.customer.dto.CustomerResponse;
import com.example.invitevip.customer.dto.CustomerSearchResponse;
import com.example.invitevip.customer.event.CustomerSearchSyncEvent;
import com.example.invitevip.customer.mapper.CustomerSearchMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {


    private final CustomerRepository customerRepository;
    private final CustomerSearchRepository customerSearchRepository;
    private final CustomerSearchMapper customerSearchMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public CustomerService(CustomerRepository customerRepository,
                           CustomerSearchRepository customerSearchRepository,
                           CustomerSearchMapper customerSearchMapper,
                           ApplicationEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.customerSearchRepository = customerSearchRepository;
        this.customerSearchMapper = customerSearchMapper;
        this.eventPublisher = eventPublisher;
    }


    public List<CustomerResponse> findAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public boolean exists(Long id) {
        return customerRepository.existsById(id);
    }

    public boolean isCodeDuplicatedForCreate(String code) {
        if (code == null || code.isBlank()) return false;
        return customerRepository.existsByInviteCode(InviteCode.of(code));
    }


    @Transactional
    public CustomerResponse save(CustomerRequest request) {

        InviteCode inviteCode = InviteCode.of(request.getCode());

        if (customerRepository.existsByInviteCode(inviteCode)) {
            throw new DuplicateCodeException("초대코드가 중복되었습니다.");
        }

        Customer customer = Customer.create(
                request.getName(),
                request.getGrade(),
                request.getPhone(),
                inviteCode,
                request.getNote()
        );

        Customer saved = customerRepository.save(customer);
        eventPublisher.publishEvent(CustomerSearchSyncEvent.upsert(saved.getId()));

        return toResponse(saved);
    }


    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));

        InviteCode newInviteCode = InviteCode.of(request.getCode());

        customerRepository.findByInviteCode(newInviteCode).ifPresent(found -> {
            if (!found.getId().equals(id)) {
                throw new DuplicateCodeException("초대코드가 중복되었습니다.");
            }
        });

        customer.update(
                request.getName(),
                request.getGrade(),
                request.getPhone(),
                newInviteCode,
                request.getNote()
        );

        eventPublisher.publishEvent(CustomerSearchSyncEvent.upsert(customer.getId()));

        return toResponse(customer);
    }

    @Transactional
    public void delete(Long id) {
        customerRepository.deleteById(id);
        eventPublisher.publishEvent(CustomerSearchSyncEvent.delete(id));
    }

    @Transactional
    public void syncAllToElasticsearch() {
        List<Customer> list = customerRepository.findAll();

        for (Customer c : list) {
            customerSearchRepository.save(customerSearchMapper.toSearchEntity(c));
        }
    }

    public List<CustomerSearchResponse> searchCustomers(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isBlank()) {
            return List.of();
        }

        return customerSearchRepository.searchAll(normalizedKeyword).stream()
                .map(customerSearchMapper::toResponse)
                .toList();
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    public CustomerResponse toResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setGrade(customer.getGrade());
        response.setPhone(customer.getPhone());
        response.setCode(customer.getCode());
        response.setNote(customer.getNote());
        return response;
    }

}
