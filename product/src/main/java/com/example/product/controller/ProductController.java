package com.example.product.controller;

import com.example.product.application.ProductFacadeService;
import com.example.product.application.ProductService;
import com.example.product.application.RedisLockService;
import com.example.product.application.dto.ProductReserveResult;
import com.example.product.controller.dto.ProductReserveRequest;
import com.example.product.controller.dto.ProductReserveResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {
    private final RedisLockService redisLockService;
    private final ProductFacadeService productFacadeService;

    public ProductController(RedisLockService redisLockService, ProductFacadeService productFacadeService) {
        this.productFacadeService = productFacadeService;
        this.redisLockService = redisLockService;
    }

    @PostMapping("/product/reserve")
    public ProductReserveResponse reserve(@RequestBody ProductReserveRequest request) {
        String key = "product:" + request.requestId();
        boolean acquiredLock = redisLockService.tryLock(key, request.requestId());

        if (!acquiredLock) {
            throw new RuntimeException("락 획득에 실패하였습니다.");
        }

        try {
            ProductReserveResult result = productFacadeService.tryReserve(request.toCommand());
            return new ProductReserveResponse(result.totalPrice());
        } finally {
            redisLockService.releaseLock(key);
        }
    }
}
