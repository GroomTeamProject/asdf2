package io.goorm.team02.core.orders.service;

import io.goorm.team02.core.orders.controller.dto.OrderRequest;
import io.goorm.team02.core.orders.controller.dto.OrderResponse;
import io.goorm.team02.core.orders.controller.dto.OrderRejectRequest;
import io.goorm.team02.core.orders.controller.dto.OrderAcceptRequest;
import io.goorm.team02.core.orders.controller.dto.OrderCancelRequest;
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
        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
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
     * 가게에서 주문 수락 (예상 조리 시간 포함)
     */
    @Transactional
    public OrderResponse acceptOrder(Long orderId, OrderAcceptRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // 도메인에서 주문 수락 처리
        order.accept(request.minCookingTime(), request.maxCookingTime());

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

    /**
     * 가게에서 주문 거절
     */
    @Transactional
    public OrderResponse rejectOrder(Long orderId, OrderRejectRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // 도메인에서 주문 거절 처리
        order.reject(request.reason());

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

    /**
     * 가게에서 조리 시작
     */
    @Transactional
    public OrderResponse startCooking(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // 도메인에서 조리 시작 처리
        order.startCooking();

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

    /**
     * 가게에서 조리 완료
     */
    @Transactional
    public OrderResponse completeCooking(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // 도메인에서 조리 완료 처리
        order.completeCooking();

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

    /**
     * 배달 완료
     */
    @Transactional
    public OrderResponse deliverOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // 도메인에서 배달 완료 처리
        order.deliver();

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

    public List<OrderResponse> getAll(Long storeId) {
        List<Order> orders = orderRepository.findAllByStoreIdWithDetails(storeId);

        // JPA 지연 로딩으로 orderItems와 options를 가져옴
        orders.forEach(order -> {
            order.getOrderItems().forEach(orderItem -> {
                orderItem.getOptions().size(); // 지연 로딩 트리거
            });
        });

        return orders.stream()
                .map(OrderResponse::from)
                .toList();
    }

    public OrderResponse getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // JPA 지연 로딩으로 orderItems와 options를 가져옴
        order.getOrderItems().forEach(orderItem -> {
            orderItem.getOptions().size(); // 지연 로딩 트리거
        });

        return OrderResponse.from(order);
    }

    /**
     * 주문 취소
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, OrderCancelRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // 도메인에서 주문 취소 처리
        order.cancel(request.cancelReason());

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

}