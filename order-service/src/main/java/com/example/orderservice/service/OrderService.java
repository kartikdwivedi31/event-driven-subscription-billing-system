package com.example.orderservice.service;

import com.example.orderservice.dto.OrderEvent;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private static final String TOPIC = "order-events";

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // 1. Save order to DB as PENDING
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .item(request.getItem())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .status(OrderStatus.PENDING)
                .build();
        
        Order savedOrder = orderRepository.save(order);

        // 2. Publish Order Created Event
        OrderEvent event = OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_CREATED")
                .orderId(savedOrder.getId())
                .customerId(savedOrder.getCustomerId())
                .amount(savedOrder.getPrice() * savedOrder.getQuantity())
                .build();

        kafkaTemplate.send(TOPIC, String.valueOf(savedOrder.getId()), event);
        log.info("Order event published for order ID: {}", savedOrder.getId());

        // 3. Return response
        return OrderResponse.builder()
                .id(savedOrder.getId())
                .customerId(savedOrder.getCustomerId())
                .item(savedOrder.getItem())
                .price(savedOrder.getPrice())
                .status(savedOrder.getStatus().name())
                .build();
    }
}
