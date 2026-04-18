package com.example.notificationservice.service;

import com.example.notificationservice.dto.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void processPaymentEvent(PaymentEvent paymentEvent) {
        log.info("Received PaymentEvent for Order ID: {} with Status: {}", 
                 paymentEvent.getOrderId(), paymentEvent.getStatus());

        if ("SUCCESS".equals(paymentEvent.getStatus())) {
            log.info("Simulating Email: Dear customer, your payment of order {} was successful!", paymentEvent.getOrderId());
        } else if ("FAILED".equals(paymentEvent.getStatus())) {
            log.info("Simulating Email: Dear customer, your payment of order {} failed. Please retry.", paymentEvent.getOrderId());
        } else {
            log.warn("Unknown payment status: {}", paymentEvent.getStatus());
        }
    }
}
