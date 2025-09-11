// 주소 등록 후 반환 값
package io.goorm.team02.core.users.controller.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserAddressResponse {
    private Long id;
    private String addressName;
    private String address;
    private String detailAddress;
    private String zipcode;
    private boolean isDefault;
}

