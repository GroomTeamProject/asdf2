// rbac 테스트용(customer만 접근 가능)
package io.goorm.team02.core.stores;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StoreController {

    @GetMapping("/api/stores/info")
    public String getStoreInfo() {
        return "This is CUSTOMER store info";
    }
}