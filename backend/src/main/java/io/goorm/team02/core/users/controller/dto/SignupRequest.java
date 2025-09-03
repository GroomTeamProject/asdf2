package io.goorm.team02.core.users.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String email;
    private String password;
    private String name;
    private String phone;
}
