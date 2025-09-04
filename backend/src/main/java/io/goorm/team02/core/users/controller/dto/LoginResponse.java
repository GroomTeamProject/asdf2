package io.goorm.team02.core.users.controller.dto;

//LoginResponse로 { "token": "JWT값" } 반환
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
}
