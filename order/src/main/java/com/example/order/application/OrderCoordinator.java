package com.example.order.application;

import com.example.order.application.dto.OrderDto;
import com.example.order.application.dto.PlaceOrderCommand;
import com.example.order.domain.CompensationRegistry;
import com.example.order.infrastructure.CompensationRegistryRepository;
import com.example.order.infrastructure.point.PointApiClient;
import com.example.order.infrastructure.point.PointUseApiRequest;
import com.example.order.infrastructure.point.PointUseCancelApiRequest;
import com.example.order.infrastructure.product.*;
import org.springframework.stereotype.Component;

@Component
public class OrderCoordinator {
    private final OrderService orderService;
    private final CompensationRegistryRepository compensationRegistryRepository;
    private final ProductApiClient productApiClient;
    private final PointApiClient pointApiClient;

    public OrderCoordinator(OrderService orderService, CompensationRegistryRepository compensationRegistryRepository, ProductApiClient productApiClient, PointApiClient pointApiClient) {
        this.orderService = orderService;
        this.compensationRegistryRepository = compensationRegistryRepository;
        this.productApiClient = productApiClient;
        this.pointApiClient = pointApiClient;
    }

    public void placeOrder(PlaceOrderCommand command) {
        orderService.request(command.orderId());
        OrderDto orderDto = orderService.getOrder(command.orderId());

        try {
            ProductBuyApiRequest productBuyApiRequest = new ProductBuyApiRequest(
                    command.orderId().toString(),
                    orderDto.orderItems().stream()
                            .map(item -> new ProductBuyApiRequest.ProductInfo(item.productId(), item.quantity()))
                            .toList()
            );

            ProductBuyApiResponse buyApiResponse = productApiClient.buy(productBuyApiRequest);

            PointUseApiRequest pointUseApiRequest = new PointUseApiRequest(
                    command.orderId().toString(),
                    1L,
                    buyApiResponse.totalPrice()
            );

            pointApiClient.use(pointUseApiRequest);
            orderService.complete(command.orderId());
        } catch (Exception e) {
            rollback(command.orderId());
        }
    }

    public void rollback(Long orderId) {
        try {
            ProductBuyCancelApiRequest productBuyCancelApiRequest = new ProductBuyCancelApiRequest(orderId.toString());
            ProductBuyCancelApiResponse productBuyCancelApiResponse = productApiClient.cancel(productBuyCancelApiRequest);
            if (productBuyCancelApiResponse.totalPrice() > 0) {
                PointUseCancelApiRequest pointUseCancelApiRequest = new PointUseCancelApiRequest(orderId.toString());
                pointApiClient.cancel(pointUseCancelApiRequest);
            }
            orderService.fail(orderId);
        } catch (Exception e) {
            compensationRegistryRepository.save(new CompensationRegistry(orderId));
            throw e;
        }
    }
}
