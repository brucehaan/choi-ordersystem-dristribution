package com.example.point.controller.dto;

import com.example.point.application.dto.PointReserveCancelCommand;

public record PointReserveCancelRequest(
        String requestId
) {
    public PointReserveCancelCommand toCommand() {
        return new PointReserveCancelCommand(requestId);
    }
}
