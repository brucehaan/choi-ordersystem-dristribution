package com.example.point.infrastructure;

import com.example.point.domain.PointReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointReservationRepository extends JpaRepository<PointReservation, Long> {
    PointReservation findByRequestId(String requestId);
}
