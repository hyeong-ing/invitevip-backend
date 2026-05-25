package com.example.invitevip.customer;

import com.example.invitevip.customer.entity.Customer;
import com.example.invitevip.customer.entity.CustomerSearchEntity;
import com.example.invitevip.customer.database.CustomerRepository;
import com.example.invitevip.customer.database.CustomerSearchRepository;
import com.example.invitevip.customer.dto.CustomerRequest;
import com.example.invitevip.customer.dto.CustomerResponse;
import com.example.invitevip.customer.dto.CustomerSearchResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {


    private final CustomerRepository customerRepository;
    private final CustomerSearchRepository customerSearchRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository,  CustomerSearchRepository customerSearchRepository) {
        this.customerRepository = customerRepository;
        this.customerSearchRepository = customerSearchRepository;
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
        return customerRepository.existsByCode(code);
    }


    @Transactional
    public CustomerResponse save(CustomerRequest request) {

        String code = request.getCode();

        if (code != null && customerRepository.existsByCode(code)) {
            throw new DuplicateCodeException("초대코드가 중복되었습니다.");
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setGrade(request.getGrade());
        customer.setPhone(request.getPhone());
        customer.setCode(request.getCode());
        customer.setNote(request.getNote());

        Customer saved = customerRepository.save(customer);

        customerSearchRepository.save(toSearchEntity(saved));

        return toResponse(saved);
    }


    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));

        String newCode = request.getCode();

        if (newCode != null) {
            customerRepository.findByCode(newCode).ifPresent(found -> {
                if (!found.getId().equals(id)) {
                    throw new DuplicateCodeException("초대코드가 중복되었습니다.");
                }
            });
        }

        customer.setName(request.getName());
        customer.setGrade(request.getGrade());
        customer.setPhone(request.getPhone());
        customer.setCode(request.getCode());
        customer.setNote(request.getNote());

        customerSearchRepository.save(toSearchEntity(customer));

        return toResponse(customer);
    }

    @Transactional
    public void delete(Long id) {
        customerRepository.deleteById(id);
        customerSearchRepository.deleteById(id);
    }

    @Transactional
    public void syncAllToElasticsearch() {
        List<Customer> list = customerRepository.findAll();

        for (Customer c : list) {
            customerSearchRepository.save(toSearchEntity(c));
        }
    }

    public List<CustomerSearchResponse> searchCustomers(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isBlank()) {
            return List.of();
        }

        return customerSearchRepository.searchAll(normalizedKeyword).stream()
                .map(this::toSearchResponse)
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

    private CustomerSearchResponse toSearchResponse(CustomerSearchEntity customer) {
        CustomerSearchResponse response = new CustomerSearchResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setGrade(customer.getGrade());
        response.setPhone(customer.getPhone());
        response.setCode(customer.getCode());
        response.setNote(customer.getNote());
        return response;
    }

    private CustomerSearchEntity toSearchEntity(Customer customer) {
        CustomerSearchEntity entity = new CustomerSearchEntity();
        entity.setId(customer.getId());
        entity.setName(customer.getName());
        entity.setGrade(customer.getGrade());
        entity.setPhone(customer.getPhone());
        entity.setCode(customer.getCode());
        entity.setNote(customer.getNote());
        entity.setNameChosung(getChosung(customer.getName()));
        return entity;
    }

    private String getChosung(String text) {
        if (text == null) return "";

        String[] CHO = {
                "ㄱ","ㄲ","ㄴ","ㄷ","ㄸ","ㄹ","ㅁ","ㅂ","ㅃ","ㅅ",
                "ㅆ","ㅇ","ㅈ","ㅉ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ"
        };

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c >= '가' && c <= '힣') {
                int uniVal = c - 0xAC00;
                int choIdx = uniVal / 588;
                sb.append(CHO[choIdx]);
            } else if (c != ' ') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
