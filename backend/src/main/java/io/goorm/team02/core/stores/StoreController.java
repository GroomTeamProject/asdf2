// rbac 테스트용(customer만 접근 가능)
package io.goorm.team02.core.stores;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StoreController {

    // customer 전용
    @GetMapping("/api/stores/info")
    public String getStoreInfo() {
        return "This is CUSTOMER store info";
    }

    // owner 전용
    @GetMapping("/api/seller/stores/info")
    public String getOwnerStoreInfo() {
        return "This is OWNER store info";
    }

    // RIDER 전용
    @GetMapping("/api/rider/info")
    public String getRiderInfo() {
        return "This is RIDER info";
    }
}