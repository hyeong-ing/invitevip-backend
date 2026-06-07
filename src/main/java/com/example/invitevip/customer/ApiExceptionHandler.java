package com.example.invitevip.customer;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DuplicateCodeException.class)
    public ResponseEntity<String> handleDup(DuplicateCodeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        if (isCustomerCodeUniqueViolation(e)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("초대코드가 중복되었습니다.");
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body("데이터 중복 또는 제약 조건 위반이 발생했습니다.");
    }

    private boolean isCustomerCodeUniqueViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getMostSpecificCause();
        String message = cause == null ? e.getMessage() : cause.getMessage();
        return message != null && message.toLowerCase().contains("uk_customer_code");
    }

}
