package com.example.invitevip.code;

import com.example.invitevip.code.entity.CodeRequest;
import com.example.invitevip.customer.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invite")
public class CodeController {

    private final CodeService codeService;

    @Autowired
    public CodeController(CodeService codeService) {
        this.codeService = codeService;
    }

    @PostMapping("/enter")
    public ResponseEntity<Customer> enterCustomer(@RequestBody CodeRequest codeRequest) {
        return codeService.findByCode(codeRequest.getCode())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
