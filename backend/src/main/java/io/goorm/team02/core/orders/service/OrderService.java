package io.goorm.team02.core.orders.service;

import io.goorm.team02.core.orders.controller.dto.OrderRequest;
import io.goorm.team02.core.orders.controller.dto.OrderResponse;
import io.goorm.team02.core.orders.controller.dto.OrderRejectRequest;
import io.goorm.team02.core.orders.controller.dto.OrderAcceptRequest;
import io.goorm.team02.core.orders.controller.dto.OrderCancelRequest;
import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.domain.OrderItem;
import io.goorm.team02.core.orders.domain.OrderItemOption;
import io.goorm.team02.core.orders.domain.enums.OrderStatus;
import io.goorm.team02.core.orders.repository.OrderRepository;
import io.goorm.team02.core.users.domain.User;
import io.goorm.team02.core.users.repository.UserinfoRepository;
import io.goorm.team02.core.stores.domain.Store;
import io.goorm.team02.core.stores.repository.StoreRepository;
import io.goorm.team02.core.menus.domain.Menu;
import io.goorm.team02.core.menus.repository.MenuRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
        // 1. 엔티티 조회 및 검증
        User user = userRepository.findById(orderRequest.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + orderRequest.userId()));

        Store store = storeRepository.findById(orderRequest.storeId())
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다: " + orderRequest.storeId()));

        // 2. 주문 아이템 생성
        List<OrderItem> orderItems = createOrderItems(orderRequest.orderItems());

        // 3. 할인 금액 계산
        BigDecimal discountAmount = calculateDiscount(user, calculateMenuTotalAmount(orderItems));

        // 4. 도메인에서 주문 생성
        Order order = Order.create(
                user,
                store,
                orderRequest.deliveryAddress(),
                orderRequest.deliveryDetailAddress(),
                orderRequest.phone(),
                orderRequest.orderMemo(),
                orderItems,
                store.getDeliveryFee(),
                discountAmount);

        // 5. 주문 검증
        order.validate();

        // 6. 저장
        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

    /**
     * 주문 아이템들 생성
     */
    private List<OrderItem> createOrderItems(List<OrderRequest.OrderItemRequest> itemRequests) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderRequest.OrderItemRequest itemRequest : itemRequests) {
            Menu menu = menuRepository.findById(itemRequest.menuId())
                    .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + itemRequest.menuId()));

            // OrderItem 생성 (임시로 null order 설정)
            OrderItem orderItem = new OrderItem();
            orderItem.setMenu(menu);
            orderItem.setMenuName(menu.getName());
            orderItem.setMenuPrice(menu.getPrice());
            orderItem.setQuantity(itemRequest.quantity());

            // 옵션 생성
            List<OrderItemOption> options = createOrderItemOptions(orderItem, itemRequest.options());
            orderItem.setOptions(options);

            // 총액 계산
            orderItem.calculateTotalPrice();

            orderItems.add(orderItem);
        }

        return orderItems;
    }

    /**
     * 주문 아이템 옵션들 생성
     */
    private List<OrderItemOption> createOrderItemOptions(OrderItem orderItem,
            List<OrderRequest.OrderItemOptionRequest> optionRequests) {
        // 옵션이 없으면 빈 리스트 반환
        if (optionRequests == null || optionRequests.isEmpty()) {
            return new ArrayList<>();
        }

        List<OrderItemOption> options = new ArrayList<>();

        for (OrderRequest.OrderItemOptionRequest optionRequest : optionRequests) {
            // 필수 필드가 null이면 해당 옵션을 건너뛰기
            if (optionRequest.optionName() == null || optionRequest.optionItemName() == null) {
                continue;
            }

            OrderItemOption option = new OrderItemOption();
            option.setOrderItem(orderItem);
            option.setOptionName(optionRequest.optionName());
            option.setOptionItemName(optionRequest.optionItemName());
            option.setAdditionalPrice(optionRequest.additionalPrice());
            options.add(option);
        }

        return options;
    }

    /**
     * 메뉴 총액 계산
     */
    private BigDecimal calculateMenuTotalAmount(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 할인 금액 계산 (복잡한 비즈니스 로직)
     */
    private BigDecimal calculateDiscount(User user, BigDecimal amount) {
        // TODO: 등급별 할인, 쿠폰, 이벤트 등 복잡한 할인 로직 구현
        // 현재는 기본 할인 없음
        return BigDecimal.ZERO;
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