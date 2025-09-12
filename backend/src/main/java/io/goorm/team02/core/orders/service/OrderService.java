package io.goorm.team02.core.orders.service;

import io.goorm.team02.core.orders.controller.dto.OrderRequest;
import io.goorm.team02.core.orders.controller.dto.OrderResponse;
import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.repository.OrderRepository;
import io.goorm.team02.core.users.domain.User;
import io.goorm.team02.core.users.repository.UserinfoRepository;
import io.goorm.team02.core.stores.domain.Store;
import io.goorm.team02.core.stores.repository.StoreRepository;
import io.goorm.team02.core.menus.domain.Menu;
import io.goorm.team02.core.menus.repository.MenuRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserinfoRepository userRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;


    // ================================
    // API Methods
    // ================================
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
    }

    public List<Order> getAllOrdersByStoreId(Long storeId) {
        return orderRepository.findAllByStoreIdWithDetails(storeId);

    }


    // ================================
    // 비즈니스 로직
    // ================================
    @Transactional
    public OrderResponse create(OrderRequest orderRequest) {
        // 1. 엔티티 참조 조회 및 검증 (서비스의 역할)
        User user = userRepository.findById(orderRequest.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + orderRequest.userId()));

        Store store = storeRepository.findById(orderRequest.storeId())
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다: " + orderRequest.storeId()));

        // 2. 메뉴 정보 조회 (주문 시점에서의 메뉴 정보 스냅샷)
        Map<Long, Menu> menuMap = getMenuMap(orderRequest.orderItems());

        // 3. Order 도메인에 위임
        Order order = Order.create(user, store, orderRequest, menuMap);

        // 4. 저장
        Order dbOrder = orderRepository.save(order);
        return OrderResponse.from(dbOrder);
    }

    /**
     * 메뉴 정보를 Map으로 조회 (서비스의 역할)
     */
    private Map<Long, Menu> getMenuMap(List<OrderRequest.OrderItemRequest> itemRequests) {
        List<Long> menuIds = itemRequests.stream()
                .map(OrderRequest.OrderItemRequest::menuId)
                .toList();

        List<Menu> menus = menuRepository.findAllById(menuIds);
        return menus.stream()
                .collect(Collectors.toMap(Menu::getId, menu -> menu));
    }

    /**
     * 가게의 모든 주문 조회
     */
    // TODO: 모든 주문 조회에서는 상세 정보를 제공할 필요 없음
    public List<OrderResponse> getAll(Long storeId) {
        List<Order> orders = getAllOrdersByStoreId(storeId);

        // JPA 지연 로딩으로 orderItems와 options를 가져옴
        // TODO: 리팩터링 필요
        orders.forEach(order -> {
            order.getOrderItems().forEach(orderItem -> {
                orderItem.getOptions().size(); // 지연 로딩 트리거
            });
        });

        return orders.stream()
                .map(OrderResponse::from)
                .toList();
    }

    /**
     * 주문 상세 조회
     */
    public OrderResponse getOrderDetail(Long orderId) {
        Order order = getOrderById(orderId);

        order.getOrderItems().forEach(orderItem -> {
            orderItem.getOptions().size(); // 지연 로딩 트리거
        });

        return OrderResponse.from(order);
    }
}