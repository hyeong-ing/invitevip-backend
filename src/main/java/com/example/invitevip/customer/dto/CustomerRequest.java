package com.example.invitevip.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest {

    @NotBlank
    @Size(max = 5)
    private String name;

    @NotBlank
    @Size(max = 10)
    private String grade;

    @NotBlank
    @Size(max = 20)
    private String phone;

    @NotBlank
    @Pattern(regexp = "\\d{4}", message = "초대코드는 숫자 4자리로 입력해야 합니다.")
    private String code;

    @Size(max = 255)
    private String note;
}
