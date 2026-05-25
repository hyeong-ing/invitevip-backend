package com.example.invitevip.customer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerResponse {

    private Long id;
    private String name;
    private String grade;
    private String phone;
    private String code;
    private String note;
}
