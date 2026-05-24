package com.example.invitevip.customer;

import com.example.invitevip.customer.database.CustomerSearchRepository;
import com.example.invitevip.customer.entity.Customer;
import com.example.invitevip.customer.entity.CustomerSearchEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerSearchRepository customerSearchRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'CUSTOMER_READ')")
    public List<Customer> effectCustomers() {
        return customerService.findAllCustomers();
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'CUSTOMER_ADD')")
    public Customer saveCustomer(@RequestBody Customer customer) {
        return customerService.save(customer);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'CUSTOMER_EDIT')")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer data) {
        if (!customerService.exists(id)) {
            return ResponseEntity.notFound().build();
        }

        Customer updated = customerService.update(id, data);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'CUSTOMER_DELETE')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        if (!customerService.exists(id)) {
            return ResponseEntity.notFound().build();
        }

        customerService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'CUSTOMER_SEARCH')")
    public List<CustomerSearchEntity> searchCustomer(@RequestParam String keyword) {
        return customerSearchRepository.searchAll(keyword);
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> sync() {
        customerService.syncAllToElasticsearch();
        return ResponseEntity.ok("sync ok");
    }
}
