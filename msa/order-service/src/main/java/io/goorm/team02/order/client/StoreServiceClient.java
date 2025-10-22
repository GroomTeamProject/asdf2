package io.goorm.team02.order.client;

import io.goorm.team02.dto.owner.menus.menucreate.MenuResponse;
import io.goorm.team02.dto.owner.stores.storemanagement.StoreResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "store-service", url = "${msa.gateway.url:http://localhost:8080}")
public interface StoreServiceClient {

    @GetMapping("/api/stores/{storeId}")
    StoreResponse getStoreById(@PathVariable Long storeId);

    @GetMapping("/api/stores/{storeId}/menus")
    List<MenuResponse> getMenusByStoreId(@PathVariable Long storeId);
}