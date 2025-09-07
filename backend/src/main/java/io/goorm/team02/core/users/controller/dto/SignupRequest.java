package io.goorm.team02.core.users.controller.dto;
import lombok.Getter;
import lombok.Setter;
import io.goorm.team02.core.users.domain.enums.UserType;

@Getter
@Setter
public class SignupRequest {
    private String email;
    private String password;
    private String passwordCheck;
    private String name;
    private String phone;
    private UserType userType;

    
}
