package com.example.invitevip.admin;

public class AdminNotFoundException extends RuntimeException {

    public AdminNotFoundException(Long id) {
        super("존재하지 않는 관리자입니다. id=" + id);
    }
}
