package io.goorm.team02.core.owner.stores.controller.dto.storemanagement;

import io.goorm.team02.core.owner.stores.domain.enums.StoreStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoreStatusRequest {

    private StoreStatus status;
    private String message; // 상태 변경 사유
}