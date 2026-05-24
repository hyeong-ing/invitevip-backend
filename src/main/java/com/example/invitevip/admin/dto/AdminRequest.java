package com.example.invitevip.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminRequest {
    private String name;
    private String username;
    private String password;
    private String role;
    private List<String> permissions;
}
