package com.example.order.application;

import com.example.order.application.dto.CreateOrderCommand;
import com.example.order.application.dto.CreateOrderResult;
import com.example.order.application.dto.OrderDto;
import com.example.order.domain.Order;
import com.example.order.domain.OrderItem;
import com.example.order.infrastructure.OrderItemRepository;
import com.example.order.infrastructure.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public OrderDto getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(orderId);

        return new OrderDto(
                orderItems.stream()
                        .map(item -> new OrderDto.OrderItem(item.getProductId(), item.getQuantity())).toList()
        );
    }

    @Transactional
    public CreateOrderResult createOrder(CreateOrderCommand command) {
        Order order = orderRepository.save(new Order());
        List<OrderItem> orderItems = command.orderItems()
                .stream()
                .map(item -> new OrderItem(order.getId(), item.productId(), item.quantity()))
                .toList();
        orderItemRepository.saveAll(orderItems);
        return new CreateOrderResult(order.getId());
    }

    @Transactional
    public void reserve(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        order.reserve();
        orderRepository.save(order);
    }

    @Transactional
    public void cancel(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        order.cancel();
        orderRepository.save(order);
    }

    @Transactional
    public void confirm(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.confirm();
        orderRepository.save(order);
    }

    @Transactional
    public void pending(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.pending();
        orderRepository.save(order);

    }
}
