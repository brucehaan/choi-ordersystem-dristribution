package com.example.point.controller.dto;

import com.example.point.application.dto.PointUseCancelCommand;

public record PointUseCancelRequest(
        String requestId
) {
    public PointUseCancelCommand toCommand() {
        return new PointUseCancelCommand(requestId);
    }
}
