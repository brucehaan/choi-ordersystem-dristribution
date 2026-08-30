package com.example.order.controller;

import com.example.order.application.OrderCoordinator;
import com.example.order.application.OrderService;
import com.example.order.application.RedisLockService;
import com.example.order.application.dto.CreateOrderResult;
import com.example.order.controller.dto.CreateOrderRequest;
import com.example.order.controller.dto.CreateOrderResponse;
import com.example.order.controller.dto.PlaceOrderRequest;
import com.example.order.domain.Order;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {
    private final OrderService orderService;
    private final RedisLockService redisLockService;
    private final OrderCoordinator orderCoordinator;

    public OrderController(OrderService orderService, RedisLockService redisLockService, OrderCoordinator orderCoordinator) {
        this.orderService = orderService;
        this.redisLockService = redisLockService;
        this.orderCoordinator = orderCoordinator;
    }

    @PostMapping("/order")
    public CreateOrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        CreateOrderResult result = orderService.createOrder(request.toCommand());
        return new CreateOrderResponse(result.orderId());
    }

    @PostMapping("/order/place")
    public void placeOrder(@RequestBody PlaceOrderRequest request) {
        String lockKey = "order:" + request.orderId();

        boolean lockAcquired = redisLockService.tryLock(lockKey, request.orderId().toString());
        if (!lockAcquired) {
            throw new RuntimeException("락 획득에 실패했습니다.");
        }
        try {
            orderService.placeOrder(request.toCommand());
        } finally {
            redisLockService.releaseLock(lockKey);
        }
    }

    @GetMapping("/order/{id}/status")
    public Order.OrderStatus getStatus(@PathVariable("id") Long id) {
        return orderService.getStatus(id);
    }
}
