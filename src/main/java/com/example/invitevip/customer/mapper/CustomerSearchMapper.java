package com.example.invitevip.customer.mapper;

import com.example.invitevip.customer.dto.CustomerSearchResponse;
import com.example.invitevip.customer.entity.Customer;
import com.example.invitevip.customer.entity.CustomerSearchEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerSearchMapper {

    public CustomerSearchResponse toResponse(CustomerSearchEntity customer) {
        CustomerSearchResponse response = new CustomerSearchResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setGrade(customer.getGrade());
        response.setPhone(customer.getPhone());
        response.setCode(customer.getCode());
        response.setNote(customer.getNote());
        return response;
    }

    public CustomerSearchEntity toSearchEntity(Customer customer) {
        CustomerSearchEntity entity = new CustomerSearchEntity();
        entity.setId(customer.getId());
        entity.setName(customer.getName());
        entity.setGrade(customer.getGrade());
        entity.setPhone(customer.getPhone());
        entity.setCode(customer.getCode());
        entity.setNote(customer.getNote());
        entity.setNameChosung(getChosung(customer.getName()));
        return entity;
    }

    private String getChosung(String text) {
        if (text == null) return "";

        String[] cho = {
                "ㄱ","ㄲ","ㄴ","ㄷ","ㄸ","ㄹ","ㅁ","ㅂ","ㅃ","ㅅ",
                "ㅆ","ㅇ","ㅈ","ㅉ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ"
        };

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c >= '가' && c <= '힣') {
                int uniVal = c - 0xAC00;
                int choIdx = uniVal / 588;
                sb.append(cho[choIdx]);
            } else if (c != ' ') {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
