package com.example.invitevip.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AuthMeResponse {

    private String username;
    private List<String> authorities;
    private boolean superAdmin;
    private boolean customerRead;
    private boolean customerSearch;
    private boolean customerAdd;
    private boolean customerEdit;
    private boolean customerDelete;
}

