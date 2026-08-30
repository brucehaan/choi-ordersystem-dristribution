package com.example.product.controller.dto;

import com.example.product.application.dto.ProductBuyCancelCommand;
import com.example.product.application.dto.ProductBuyCommand;

public record ProductBuyCancelRequest(
        String requestId
) {
    public ProductBuyCancelCommand toCommand() {
        return new ProductBuyCancelCommand(requestId);
    }
}
