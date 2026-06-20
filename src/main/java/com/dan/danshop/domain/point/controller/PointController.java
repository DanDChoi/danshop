package com.dan.danshop.domain.point.controller;

import com.dan.danshop.domain.point.dto.PointHistoryResponse;
import com.dan.danshop.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Long>> getBalance() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(Map.of("pointBalance", pointService.getBalance(userId)));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PointHistoryResponse>> getHistory() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(pointService.getHistory(userId));
    }
}
