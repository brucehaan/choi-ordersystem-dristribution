package com.example.monolithic.order.application;

import com.example.monolithic.order.application.dto.CreateOrderCommand;
import com.example.monolithic.order.application.dto.CreateOrderResult;
import com.example.monolithic.order.application.dto.PlaceOrderCommand;
import com.example.monolithic.order.domain.Order;
import com.example.monolithic.order.domain.OrderItem;
import com.example.monolithic.order.infrastructure.OrderItemRepository;
import com.example.monolithic.order.infrastructure.OrderRepository;
import com.example.monolithic.point.application.PointService;
import com.example.monolithic.product.application.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PointService pointService;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, PointService pointService, ProductService productService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.pointService = pointService;
        this.productService = productService;
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
    public void placeOrder(PlaceOrderCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new RuntimeException("주문정보가 존재하지 않습니다."));

        if (order.getStatus() == Order.OrderStatus.COMPLETED) {
            return;
        }
        Long totalPrice = 0L;

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(order.getId());

        for (OrderItem item : orderItems) {
            Long price = productService.buy(item.getProductId(), item.getQuantity());
            totalPrice += price;
        }
        pointService.use(1L, totalPrice);

        order.complete();
        orderRepository.save(order);
    }
}
