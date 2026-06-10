package com.example.invitevip.customer;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super("고객을 찾을 수 없습니다. id=" + id);
    }
}
