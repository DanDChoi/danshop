package com.dan.danshop.domain.point.repository;

import com.dan.danshop.domain.point.entity.PointHistory;
import com.dan.danshop.domain.point.entity.PointType;
import com.dan.danshop.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findByUserOrderByCreatedAtDesc(User user);
    List<PointHistory> findByOrderIdAndTypeIn(Long orderId, List<PointType> types);
}
