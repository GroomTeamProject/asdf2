// 새로운 주소 등록 요청 값
package io.goorm.team02.core.users.controller.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAddressRequest {
    private String addressName;
    private String address;
    private String detailAddress;
    private String zipcode;
    private boolean isDefault;
}
