package io.goorm.team02.common.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class UserVerifyResponse {
    private Long id;
    private String email;
    private String name;
    private String userType;
    private boolean active; // ✅ isActive(X)
}

