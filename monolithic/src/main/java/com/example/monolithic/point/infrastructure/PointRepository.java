package com.example.monolithic.point.infrastructure;

import com.example.monolithic.point.domain.Point;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Long> {
    Point findByUserId(Long userId);
}
