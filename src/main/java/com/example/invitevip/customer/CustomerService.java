package com.example.invitevip.customer;

import com.example.invitevip.customer.entity.Customer;
import com.example.invitevip.customer.entity.CustomerSearchEntity;
import com.example.invitevip.customer.database.CustomerRepository;
import com.example.invitevip.customer.database.CustomerSearchRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {


    private CustomerRepository customerRepository;
    private final CustomerSearchRepository customerSearchRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository,  CustomerSearchRepository customerSearchRepository) {
        this.customerRepository = customerRepository;
        this.customerSearchRepository = customerSearchRepository;
    }


    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    public boolean exists(Long id) {
        return customerRepository.existsById(id);
    }

    public boolean isCodeDuplicatedForCreate(String code) {
        if (code == null || code.isBlank()) return false;
        return customerRepository.existsByCode(code);
    }


    @Transactional
    public Customer save(Customer customer) {

        String code = customer.getCode();

        if (code != null && customerRepository.existsByCode(code)) {
            throw new DuplicateCodeException("초대코드가 중복되었습니다.");
        }


        Customer saved = customerRepository.save(customer);

        CustomerSearchEntity ela = new CustomerSearchEntity();
        ela.setId(saved.getId());
        ela.setName(saved.getName());
        ela.setGrade(saved.getGrade());
        ela.setPhone(saved.getPhone());
        ela.setCode(saved.getCode());
        ela.setNote(saved.getNote());
        ela.setNameChosung(getChosung(saved.getName()));

        customerSearchRepository.save(ela);

        return saved;
    }


    @Transactional
    public Customer update(Long id, Customer updateData) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));

        String newCode = updateData.getCode();

        if (newCode != null) {
            customerRepository.findByCode(newCode).ifPresent(found -> {
                if (!found.getId().equals(id)) {
                    throw new DuplicateCodeException("초대코드가 중복되었습니다.");
                }
            });
        }

        customer.setName(updateData.getName());
        customer.setGrade(updateData.getGrade());
        customer.setPhone(updateData.getPhone());
        customer.setCode(updateData.getCode());
        customer.setNote(updateData.getNote());

        CustomerSearchEntity ela = new CustomerSearchEntity();
        ela.setId(customer.getId());
        ela.setName(customer.getName());
        ela.setGrade(customer.getGrade());
        ela.setPhone(customer.getPhone());
        ela.setCode(customer.getCode());
        ela.setNote(customer.getNote());
        ela.setNameChosung(getChosung(customer.getName()));

        customerSearchRepository.save(ela);

        return customer;
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
            CustomerSearchEntity e = new CustomerSearchEntity();
            e.setId(c.getId());
            e.setName(c.getName());
            e.setGrade(c.getGrade());
            e.setPhone(c.getPhone());
            e.setCode(c.getCode());
            e.setNote(c.getNote());
            e.setNameChosung(getChosung(c.getName()));

            customerSearchRepository.save(e);
        }
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
