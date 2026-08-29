package com.example.order.application;

import com.example.order.application.dto.OrderDto;
import com.example.order.application.dto.PlaceOrderCommand;
import com.example.order.infrastructure.point.PointApiClient;
import com.example.order.infrastructure.point.dto.PointReserveApiRequest;
import com.example.order.infrastructure.point.dto.PointReserveCancelApiRequest;
import com.example.order.infrastructure.point.dto.PointReserveConfirmApiRequest;
import com.example.order.infrastructure.product.ProductApiClient;
import com.example.order.infrastructure.product.dto.ProductReserveApiRequest;
import com.example.order.infrastructure.product.dto.ProductReserveApiResponse;
import com.example.order.infrastructure.product.dto.ProductReserveCancelApiRequest;
import com.example.order.infrastructure.product.dto.ProductReserveConfirmApiRequest;
import org.springframework.stereotype.Component;

@Component
public class OrderCoordinator {
    private final OrderService orderService;
    private final ProductApiClient productApiClient;
    private final PointApiClient pointApiClient;

    public OrderCoordinator(OrderService orderService, ProductApiClient productApiClient, PointApiClient pointApiClient) {
        this.orderService = orderService;
        this.productApiClient = productApiClient;
        this.pointApiClient = pointApiClient;
    }

    public void placeOrder(PlaceOrderCommand command) {
        reserve(command.orderId());
        confirm(command.orderId());
    }

    private void reserve(Long orderId) {
        String requestId = orderId.toString();
        orderService.reserve(orderId);

        try {
            OrderDto orderInfo = orderService.getOrder(orderId);
            ProductReserveApiRequest productReserveApiRequest = new ProductReserveApiRequest(
                    requestId,
                    orderInfo.orderItems().stream()
                            .map(
                                    orderItem -> new ProductReserveApiRequest.ReserveItem(
                                            orderItem.productId(),
                                            orderItem.quantity()
                                    )
                            ).toList()
            );
            ProductReserveApiResponse productReserveApiResponse = productApiClient.reserve(productReserveApiRequest);
            PointReserveApiRequest pointReserveApiRequest = new PointReserveApiRequest(
                    requestId,
                    1L,
                    productReserveApiResponse.totalPrice()
            );

            pointApiClient.reservePoint(pointReserveApiRequest);
        } catch (Exception e) {
            orderService.cancel(orderId);
            productApiClient.cancel(new ProductReserveCancelApiRequest(requestId));
            pointApiClient.cancelPoint(new PointReserveCancelApiRequest(requestId));
        }
    }

    public void confirm(Long orderId) {
        String requestId = orderId.toString();
        try {
            productApiClient.confirm(new ProductReserveConfirmApiRequest(requestId));
            pointApiClient.confirmPoint(new PointReserveConfirmApiRequest(requestId));
            orderService.confirm(orderId);
        } catch (Exception e) {
            orderService.pending(orderId);
            throw e;
        }
    }
}
