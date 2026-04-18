package com.example.paymentservice.service;

import com.example.paymentservice.dto.OrderEvent;
import com.example.paymentservice.dto.PaymentEvent;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    private static final String PAYMENT_TOPIC = "payment-events";

    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    @Transactional
    public void processOrderEvent(OrderEvent orderEvent) {
        log.info("Received OrderEvent for Order ID: {}", orderEvent.getOrderId());

        if (!"ORDER_CREATED".equals(orderEvent.getEventType())) {
            log.info("Ignoring event type: {}", orderEvent.getEventType());
            return;
        }

        // Simulate payment processing (80% success rate)
        boolean isSuccess = Math.random() > 0.2;
        PaymentStatus status = isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        Payment payment = Payment.builder()
                .orderId(orderEvent.getOrderId())
                .customerId(orderEvent.getCustomerId())
                .amount(orderEvent.getAmount())
                .status(status)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment saved with ID: {} and Status: {}", savedPayment.getId(), status);

        // Publish PaymentEvent
        PaymentEvent paymentEvent = PaymentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PAYMENT_" + status.name())
                .paymentId(savedPayment.getId())
                .orderId(savedPayment.getOrderId())
                .status(status.name())
                .build();

        kafkaTemplate.send(PAYMENT_TOPIC, String.valueOf(savedPayment.getOrderId()), paymentEvent);
        log.info("Published PaymentEvent for Order ID: {}", savedPayment.getOrderId());
    }
}
