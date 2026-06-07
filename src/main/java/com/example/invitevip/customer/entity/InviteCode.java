package com.example.invitevip.customer.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InviteCode {

    private String value;

    private InviteCode(String value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException("초대코드는 1자 이상 4자 이하로 입력해야 합니다.");
        }

        this.value = value.trim();
    }

    public static InviteCode of(String value) {
        return new InviteCode(value);
    }

    public static boolean isValid(String value) {
        return value != null && !value.isBlank() && value.trim().length() <= 4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof InviteCode that))
            return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
