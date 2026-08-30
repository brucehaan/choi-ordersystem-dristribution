package com.example.product.controller;

import com.example.product.application.ProductService;
import com.example.product.application.RedisLockService;
import com.example.product.application.dto.ProductBuyCancelResult;
import com.example.product.application.dto.ProductBuyResult;
import com.example.product.controller.dto.ProductBuyCancelRequest;
import com.example.product.controller.dto.ProductBuyCancelResponse;
import com.example.product.controller.dto.ProductBuyRequest;
import com.example.product.controller.dto.ProductBuyResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {
    private final ProductService productService;
    private final RedisLockService redisLockService;

    public ProductController(ProductService productService, RedisLockService redisLockService) {
        this.productService = productService;
        this.redisLockService = redisLockService;
    }

    @PostMapping("/product/buy")
    public ProductBuyResponse buy(@RequestBody ProductBuyRequest request) {
        String lockKey = "product:orchestration:" + request.requestId();

        boolean lockAcquired = redisLockService.tryLock(lockKey, request.requestId());
        if (!lockAcquired) {
            throw new RuntimeException("락 획득에 실패하였습니다.");
        }
        try {
            ProductBuyResult buyResult = productService.buy(request.toCommand());
            return new ProductBuyResponse(buyResult.totalPrice());
        } finally {
            redisLockService.releaseLock(lockKey);
        }
    }

    @PostMapping("/product/buy/cancel")
    public ProductBuyCancelResponse cancel(@RequestBody ProductBuyCancelRequest request) {
        String lockKey = "product:orchestration:" + request.requestId();

        boolean lockAcquired = redisLockService.tryLock(lockKey, request.requestId());
        if (!lockAcquired) {
            throw new RuntimeException("락 획득에 실패하였습니다.");
        }

        try {
            ProductBuyCancelResult cancelResult = productService.cancel(request.toCommand());
            return new ProductBuyCancelResponse(cancelResult.totalPrice());
        } finally {
            redisLockService.releaseLock(lockKey);
        }
    }
}
