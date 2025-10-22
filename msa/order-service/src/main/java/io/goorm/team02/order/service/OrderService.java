package io.goorm.team02.order.service;

import static io.goorm.team02.order.service.OrderStatusService.ORDER_EVENTS_TOPIC;

import io.goorm.team02.dto.orders.*;
import io.goorm.team02.order.entity.Order;
import io.goorm.team02.order.entity.enums.OrderStatus;
import io.goorm.team02.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.goorm.team02.kafka.client.EventPublisher;
import io.goorm.team02.order.client.StoreServiceClient;
import io.goorm.team02.order.event.OrderCreatedEvent;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;
    private final StoreServiceClient storeServiceClient;
    private final OrderMapper orderMapper;

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

    public List<Order> getAllOrdersByUserId(Long userId) {
        return orderRepository.findAllByUserId(userId);
    }

    // ================================
    // Business Logic
    // ================================
    @Transactional
    public Order create(OrderRequest orderRequest, Long userId) {
        // 1. 가게 정보 조회
        var storeInfo = storeServiceClient.getStoreById(orderRequest.storeId());
        
        // 2. 메뉴 정보 조회 (주문 시점에서의 메뉴 정보 스냅샷)
        // TODO: 실제 메뉴 정보 사용 (스냅샷)

        // 3. Order 도메인에 생성 요청
        Order order = Order.create(orderRequest, userId, orderRequest.storeId(), 0);
        
        // 4. 가게 정보 설정 (비정규화)
        order.setStoreName(storeInfo.getName());
        order.setStorePhone(storeInfo.getPhone());
        order.setStoreAddress(storeInfo.getAddress());
        order.setStoreDetailAddress(storeInfo.getDetailAddress());

        // 5. 저장
        Order savedOrder = orderRepository.save(order);

        // 6. 주문 생성 이벤트 발행 - OrderCreatedEvent
        eventPublisher.publish(ORDER_EVENTS_TOPIC, new OrderCreatedEvent(savedOrder));

        return savedOrder;
    }

    /**
     * 주문 목록 조회 (페이지네이션 지원)
     */
    public Page<Order> getAllByParams(OrderSearchRequest searchRequest, Long userId) {
        Page<Order> orders;
        Pageable pageable = PageRequest.of(searchRequest.getPageOrDefault(), searchRequest.getSizeOrDefault());

        // 검색 조건에 따라 다른 메서드 호출
        if (searchRequest.hasStoreId()) {
            orders = orderRepository.findAllByStoreIdWithPagination(searchRequest.getStoreId(), pageable);
        } else if (searchRequest.hasUserId()) {
            orders = orderRepository.findAllByUserIdWithPagination(userId, pageable);
        } else {
            // 모든 주문 조회
            orders = orderRepository.findAllWithPagination(pageable);
        }

        // JPA 지연 로딩으로 orderItems와 options를 가져옴
        orders.getContent().forEach(order -> {
            order.getOrderItems().forEach(orderItem -> {
                orderItem.getOptions().size(); // 지연 로딩 트리거
            });
        });

        return orders;
    }

    /**
     * 주문 상세 조회 (권한 검증 포함)
     */
    public Order getOrderDetail(Long orderId, Long userId) {
        Order order = getOrderById(orderId);

        // 권한 검증: 주문 소유자만 조회 가능
        if (!order.getUserId().equals(userId)) {
            throw new IllegalStateException("본인의 주문만 조회할 수 있습니다.");
        }

        order.getOrderItems().forEach(orderItem -> {
            orderItem.getOptions().size(); // 지연 로딩 트리거
        });

        return order;
    }

    /**
     * 픽업 가능한 주문 목록 조회 (READY 상태)
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAvailableOrders(Long storeId) {
        List<Order> orders;

        if (storeId != null) {
            // 특정 가게의 READY 상태 주문들
            orders = orderRepository.findByStoreIdAndStatus(storeId, OrderStatus.READY);
        } else {
            // 모든 가게의 READY 상태 주문들
            orders = orderRepository.findByStatus(OrderStatus.READY);
        }

        // 지연 로딩 처리
        orders.forEach(this::loadOrderDetails);

        return orders.stream()
                .map(Order::toResponse)
                .toList();
    }

    public OrderResponseForDelivery getOrderDetailForDelivery(Long orderId) {
        Order order = getOrderById(orderId);
        return orderMapper.toResponseForDelivery(order);
    }

    /**
     * 주문 상세 정보 지연 로딩 처리 (중복 코드 제거)
     */
    private void loadOrderDetails(Order order) {
        order.getOrderItems().forEach(orderItem -> {
            orderItem.getOptions().size(); // 지연 로딩 트리거
        });
    }

    /**
     * 가게별 대시보드 데이터 조회
     */
    public OrderDashboardDto getDashboardData(Long storeId) {

        Long todayOrderCount = getTodayOrderCount(storeId);
        BigDecimal todayRevenue = getTodayRevenue(storeId);
        Long totalOrderCount = getTotalOrderCount(storeId);
        List<RecentOrderDto> recentOrders = getRecentOrders(storeId, 5);

        // TODO: 리뷰 데이터는 Review 서비스에서 가져와야 함
        BigDecimal averageRating = BigDecimal.valueOf(4.5); // 임시값
        Long reviewCount = 100L; // 임시값

        OrderDashboardDto result = OrderDashboardDto.builder()
                .todayOrderCount(todayOrderCount)
                .todayRevenue(todayRevenue)
                .totalOrderCount(totalOrderCount)
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .recentOrders(recentOrders)
                .build();

        return result;
    }

    /**
     * 가게별 오늘 주문 개수 조회
     */
    public Long getTodayOrderCount(Long storeId) {
        return orderRepository.countTodayOrdersByStoreId(storeId);
    }

    /**
     * 가게별 오늘 매출 조회 (배달 완료된 주문만)
     */
    public BigDecimal getTodayRevenue(Long storeId) {
        BigDecimal revenue = orderRepository.getTodayRevenueByStoreId(storeId);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    /**
     * 가게별 총 주문 개수 조회
     */
    public Long getTotalOrderCount(Long storeId) {
        return orderRepository.countTotalOrdersByStoreId(storeId);
    }

    /**
     * 가게별 최근 주문 조회
     */
    public List<RecentOrderDto> getRecentOrders(Long storeId, int limit) {

        Pageable pageable = PageRequest.of(0, limit);
        List<Order> orders = orderRepository.findRecentOrdersByStoreId(storeId, pageable);

        return orders.stream()
                .map(this::convertToRecentOrderDto)
                .collect(Collectors.toList());
    }


    // ================================
    // Internal Methods
    // ================================
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문이 존재하지 않습니다."));
        if (order.getStatus() == OrderStatus.COOKING) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        } else {
            throw new RuntimeException("이미 완료된 주문은 취소할 수 없습니다.");
        }
    }

    @Transactional
    public void updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문이 존재하지 않습니다."));
        order.setStatus(status);
        orderRepository.save(order);
    }

    /**
     * Order Entity를 RecentOrderDto로 변환
     */
    private RecentOrderDto convertToRecentOrderDto(Order order) {
        // 주문 아이템들 변환
        List<OrderItemDto> items = order.getOrderItems().stream()
                .map(orderItem -> OrderItemDto.builder()
                        .name(orderItem.getMenuName())
                        .quantity(orderItem.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return RecentOrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName("고객" + order.getUserId())
                .total(BigDecimal.valueOf(order.getTotalAmount()))
                .status(order.getStatus().name())
                .orderTime(order.getCreatedAt())
                .items(items)
                .build();
    }

}
