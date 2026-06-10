package com.example.invitevip.code.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeRequest {
    @NotBlank
    @Pattern(regexp = "\\d{4}", message = "초대코드는 숫자 4자리로 입력해야 합니다.")
    private String code;
}
