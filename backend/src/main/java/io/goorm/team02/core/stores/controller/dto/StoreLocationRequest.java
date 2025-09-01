// StoreLocationRequest.java
package io.goorm.team02.core.stores.controller.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoreLocationRequest {

    private String address;
    private String detailAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<String> deliveryAreas;
}