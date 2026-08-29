package com.example.order.infrastructure.product;

import com.example.order.infrastructure.product.dto.ProductReserveApiRequest;
import com.example.order.infrastructure.product.dto.ProductReserveApiResponse;
import com.example.order.infrastructure.product.dto.ProductReserveCancelApiRequest;
import com.example.order.infrastructure.product.dto.ProductReserveConfirmApiRequest;
import org.springframework.web.client.RestClient;

public class ProductApiClient {
    private final RestClient restClient;

    public ProductApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public ProductReserveApiResponse reserve(ProductReserveApiRequest request) {
        return restClient.post()
                .uri("/product/reserve")
                .body(request)
                .retrieve()
                .body(ProductReserveApiResponse.class);
    }

    public void confirm(ProductReserveConfirmApiRequest request) {
        restClient.post()
                .uri("/product/confirm")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void cancel(ProductReserveCancelApiRequest request) {
        restClient.post()
                .uri("/product/cancel")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
