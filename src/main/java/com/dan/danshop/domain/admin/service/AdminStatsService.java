package com.dan.danshop.domain.admin.service;

import com.dan.danshop.domain.admin.dto.OrderStatusStatResponse;
import com.dan.danshop.domain.admin.dto.ProductSalesResponse;
import com.dan.danshop.domain.admin.dto.SalesStatResponse;
import com.dan.danshop.domain.admin.repository.AdminStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final AdminStatsRepository adminStatsRepository;

    @Transactional(readOnly = true)
    public SalesStatResponse getSalesStat(LocalDateTime from, LocalDateTime to) {
        return adminStatsRepository.getSalesStat(from, to);
    }

    @Transactional(readOnly = true)
    public List<ProductSalesResponse> getTopProductsBySales(int limit) {
        return adminStatsRepository.getTopProductsBySales(limit);
    }

    @Transactional(readOnly = true)
    public List<OrderStatusStatResponse> getOrderStatusStat() {
        return adminStatsRepository.getOrderStatusStat();
    }
}
