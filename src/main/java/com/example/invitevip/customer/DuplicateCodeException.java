package com.example.invitevip.customer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateCodeException extends RuntimeException {
    public  DuplicateCodeException(String message) {
        super(message);
    }

}
