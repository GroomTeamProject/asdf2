package io.goorm.team02.core.stores.controller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoreContactRequest {

    private String phone;
    private String email;
    private String contactPerson;
}