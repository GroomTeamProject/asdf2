@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventHandler {

    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;
    private final PaymentService paymentService;

    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    public void handleOrderEvent(OrderCreatedEvent event) {
        log.info("[PaymentEventHandler] 주문 이벤트 수신: {}", event);

        boolean result = paymentService.processPayment(event);

        if (result) {
            PaymentCompletedEvent completedEvent = PaymentCompletedEvent.builder()
                    .orderId(event.getOrderId())
                    .paymentKey("PAY_" + event.getOrderId())
                    .amount(event.getAmount())
                    .status("COMPLETED")
                    .build();

            kafkaTemplate.send("payment-events", completedEvent.getOrderId(), completedEvent);
            log.info("[PaymentEventHandler] 결제 완료 이벤트 발행: {}", completedEvent);
        } else {
            log.warn("[PaymentEventHandler] 결제 실패: {}", event.getOrderId());
        }
    }
}
