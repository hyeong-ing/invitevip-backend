package com.example.invitevip.customer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiExceptionHandler {

    @ExceptionHandler(DuplicateCodeException.class)
    public ResponseEntity<String> handleDup(DuplicateCodeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }


}
